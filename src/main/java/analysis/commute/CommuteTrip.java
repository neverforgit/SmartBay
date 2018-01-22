package analysis.commute;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.network.Link;

import java.util.HashMap;

/**
 * Created by Andrew A. Campbell on 9/27/16.
 */
public class CommuteTrip {

    private String origActivity;
    private String destActivity;
    private double fwyCutOffSpeed = 33.3;  //cutoff speed for defining freeway travel
    private double congestionCutOffSpeed = 15.65;
    private double tripStartTime;  // time vehicle enters traffic
    private double tripEndTime;  // time vehicle leaves traffic
    private boolean calcFreeways = true;

    // Commute summary fields
    private double totalTime = 0;
    private double totalDist = 0;

    private double delayTime = 0;
    private double delayDist = 0;

    private double congestedTime = 0;
    private double congestedDist = 0;

    // Commute summary fields - freeway only
    private double freewayTotalTime = 0;
    private double freewayTotalDist = 0;

    private double freewayDelayDist = 0;
    private double freewayDelayTime = 0;

    private double freewayCongestedTime = 0;
    private double freewayCongestedDist = 0;

    // Temp holders
    private Double linkEnterTime;  // use Double b/c needs to be nullable
    private Id<Link> currentLinkId;

    // constructors
    public CommuteTrip(String origActivity, double fwySpeed, double congestionSpeed){
        this.origActivity = origActivity;
        this.fwyCutOffSpeed = fwySpeed;
        this.congestionCutOffSpeed = congestionSpeed;
    }

    // setters
    public void setCalcFreeways(boolean value){
        this.calcFreeways = value;
    }


    // Updaters
    public void enterTraffic(VehicleEntersTrafficEvent event){
        this.tripStartTime = event.getTime();
        this.linkEnterTime = event.getTime();
        this.currentLinkId = event.getLinkId();
    }


    public void enterLink(LinkEnterEvent event){
        this.linkEnterTime = event.getTime();
        this.currentLinkId = event.getLinkId();
    }

    public void leaveLink(LinkLeaveEvent event, Link link){
        // Link attributes
        double freeSpeed = link.getFreespeed();
        double linkLength = link.getLength();
        double freeTime = linkLength/freeSpeed;  // free flow time
        // Experienced link travel time
        double travelTime = event.getTime() - this.linkEnterTime;
        double travelSpeed = linkLength / travelTime;
        // Update fields
        // Total time/dist
        this.totalTime += travelTime;
        this.totalDist += linkLength;
        // Delay time/dist
        if (travelTime - freeTime > 0){
            this.delayTime += (travelTime - freeTime);
            this.delayDist += linkLength;
        }
        // Congestion
        if (travelSpeed < this.congestionCutOffSpeed){
            this.congestedTime += (travelTime - freeTime);
            this.congestedDist += linkLength;
        }
        // Freeway
        if (this.calcFreeways && link.getFreespeed() >= this.fwyCutOffSpeed){
            // Update fields
            this.freewayTotalTime += travelTime;
            this.freewayTotalDist += linkLength;
            if (travelTime - freeTime > 0){
                this.freewayDelayTime += (travelTime - freeTime);
                this.freewayDelayDist += linkLength;
            }
            if (travelSpeed < this.congestionCutOffSpeed){
                this.freewayCongestedTime += (travelTime - freeTime);
                this.freewayCongestedDist += linkLength;
            }
        }
        this.linkEnterTime = null;  // reset that way we will throw a nullpointer if we ever try to match a link leave
        // with the wrong link enter time.
    }

    /**
     * A VehicleLeavesTrafficEvent is just the last LinkLeaveEvent. Thus we set the final trip end time, and then call
     * leavesLink the LinkLeaveEvent we created.
     * @param event
     * @param link
     */
    public void leaveTraffic(VehicleLeavesTrafficEvent event, Link link){
        this.tripEndTime = event.getTime();
        LinkLeaveEvent leaveEvent = new LinkLeaveEvent(event.getTime(), event.getVehicleId(), event.getLinkId());
        this.leaveLink(leaveEvent, link);
    }

    public void startActivity(ActivityStartEvent event){

    }

    /**
     * Returns a map of all the attributes w/out restricting to freeway links.
     * @return
     */
    public HashMap<String, Double> getAllAttributes(){
        HashMap<String, Double> out = new HashMap<String, Double>();
        out.put("startTime", this.tripStartTime);
        out.put("totalTime", this.totalTime);
        out.put("totalDist", this.totalDist);
        out.put("totalDelay", this.delayTime);
        out.put("delayDist", this.delayDist);
        out.put("congestedTime", this.congestedTime);
        out.put("congestedDist", this.congestedDist);
        return  out;
    }

    /**
     * Returns a map of all the attributes w/out restricting to freeway links.
     * @return
     */
    public HashMap<String, Double> getFreewayAttributes(){
        HashMap<String, Double> out = new HashMap<String, Double>();
        out.put("startTime", this.tripStartTime);
        out.put("totalTime", this.freewayTotalTime);
        out.put("totalDist", this.freewayTotalDist);
        out.put("totalDelay", this.freewayDelayTime);
        out.put("delayDist", this.freewayDelayDist);
        out.put("congestedTime", this.freewayCongestedTime);
        out.put("congestedDist", this.freewayCongestedDist);
        return  out;
    }

    public void setCongestionSpeed(double speed){
        this.fwyCutOffSpeed = speed;
    }
}
