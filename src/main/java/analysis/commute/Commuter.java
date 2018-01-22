package analysis.commute;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;

/**
 * Created by Andrew A. Campbell on 9/26/16.
 *
 * Data container for a single agent. Stores the attributes of their home-work and work-home trips.
 */
public class Commuter {

    // Commuter fields
    private String home = "Home";
    private String work = "Work";
    private Network network;
    private boolean calcFreeways = true;
    protected double fwyCutOffSpeed = 33.3;
    protected double congestionCutOffSpeed = 16.55;
    protected boolean trackingTrip = false;
    protected Id<Vehicle> currentVehicleId;


    // Commute OD
    private Coord homeCoord;
    private Coord workCoord;

    // Temp trips. We don't know if they are commute trips until we see the dest activity.
    private CommuteTrip tempH2WTrip;
    private boolean trackingH2W = false;
    private CommuteTrip tempW2HTrip;
    private boolean trackingW2H = false;

    // Final trips.
    private CommuteTrip finalH2WTrip;
    private CommuteTrip finalW2HTrip;


//    public Commuter(Network network){
//        this.network = network;
//    }

    public Commuter(Network network, String home, String work, double fwySpeed, double congestionSpeed){
        this.network = network;
        this.home = home;
        this.work = work;
        this.fwyCutOffSpeed = fwySpeed;
        this.congestionCutOffSpeed = congestionSpeed;
    }

    //TODO add ability to filter by freeway links

    /**
     * ActivityEndsEvent marks the start a of a new trip. This only gets called if the activity type matches one of
     * types defining a commute (i.e. home or work). But we won't know if this is a commute trip until we see which
     * type of activity is started at the end of the trip. So initialize temp trips to track for now.
     * @param event
     */
    public void activityEnds(ActivityEndEvent event){
        String origActivity = event.getActType();
        Coord coord = this.network.getLinks().get(event.getLinkId()).getCoord();
        if (origActivity.equals(this.home)){
            this.tempH2WTrip = new CommuteTrip(origActivity, this.fwyCutOffSpeed, this.congestionCutOffSpeed);
            this.homeCoord = coord;
            this.trackingH2W = true;
        }
        else if (origActivity.equals(this.work)){
            this.tempW2HTrip = new CommuteTrip(origActivity, this.fwyCutOffSpeed, this.congestionCutOffSpeed);
            this.workCoord = coord;
            this.trackingW2H = true;
        }
        this.trackingTrip = true;
    }

    /**
     * Updates fields for when agent enters traffic at the beginning of trip. This is when we start measuring time.
     * @param event
     */
    public void entersTraffic(VehicleEntersTrafficEvent event){
        this.currentVehicleId = event.getVehicleId();
        if (this.trackingH2W){
            this.tempH2WTrip.enterTraffic(event);
        } else if (this.trackingW2H) {
            this.tempW2HTrip.enterTraffic(event);
        }
    }

    /**
     *
     * @param event
     */
    public void entersLink(LinkEnterEvent event){
        if (this.trackingH2W){
            this.tempH2WTrip.enterLink(event);
        } else {
            this.tempW2HTrip.enterLink(event);
        }
    }

    /**
     *
     * @param event
     */
    public void leavesLink(LinkLeaveEvent event){
        Link link = this.network.getLinks().get(event.getLinkId());
        if (this.trackingH2W){
            this.tempH2WTrip.leaveLink(event, link);
        } else {
            this.tempW2HTrip.leaveLink(event, link);
        }
    }

    /**
     *
     * @param event
     */
    public void leavesTraffic(VehicleLeavesTrafficEvent event){
        Link link = this.network.getLinks().get(event.getLinkId());
        if (this.trackingH2W){
            this.tempH2WTrip.leaveTraffic(event, link);
        } else {
            this.tempW2HTrip.leaveTraffic(event, link);
        }
    }

    /**
     *
     * @param event
     */
    public void startActivity(ActivityStartEvent event) {
        String destActivity = event.getActType();
        if (this.trackingH2W) {
            if (destActivity.equals(this.work)) {
                // process a final home-work commute
                this.tempH2WTrip.startActivity(event);
                this.finalH2WTrip = this.tempH2WTrip;
                this.trackingH2W = false;
                this.tempW2HTrip = null;
                this.trackingTrip = false;
            } else {
                //not a matching trip end. Need to reset: 1) this.trackingH2W 2) temp tripH2W
                this.trackingH2W = false;
                this.tempW2HTrip = null;
                this.trackingTrip = false;
            }
        } else if (this.trackingW2H) {
            if (destActivity.equals(this.home)) {
                // process final work-home activity
                this.tempW2HTrip.startActivity(event);
                this.finalW2HTrip = this.tempW2HTrip;
                this.trackingW2H = false;
                this.tempW2HTrip = null;
                this.trackingTrip = false;
            } else {
                // not a mathcing trip end
                this.trackingW2H = false;
                this.tempW2HTrip = null;
                this.trackingTrip = false;
            }
        }
    }

    public HashMap<String, Coord> getHWCoords(){
        HashMap<String, Coord> out = new HashMap<>();
        out.put("home", this.homeCoord);
        out.put("work", this.workCoord);
        return out;
    }

    public CommuteTrip getTripH2W(){
        return this.finalH2WTrip;
    }

    public CommuteTrip getTripW2H(){
        return this.finalW2HTrip;
    }
}
