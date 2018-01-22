package analysis.commute;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Andrew A. Campbell on 9/26/16.
 */
public class CommuteHandler implements ActivityEndEventHandler, ActivityStartEventHandler, LinkEnterEventHandler, LinkLeaveEventHandler,
		VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {
    private String home = "Home";
    private String work = "Work";
    private HashMap<Id<Person>, Commuter> commuters = new HashMap<>();
    private HashMap<Id<Vehicle>, Id<Person>> vehId2PersonId = new HashMap<>();
    private Network network;
    private boolean calcFreeways = true;
    private double fwyCutOffSpeed = 33.3;  // min freeflow speed for a link to be considered "freeway'
    private double congestionCutOffSpeed = 15.65;  // min speed for flow to be considered "congested"

    /**
     * By default, we calculate values for all links and freeway links. Uses 33.3 meter/sec as the default cutoff for
     * defining a freeway link.
     * @param inputNetwork
     */
    public CommuteHandler(String inputNetwork){
//        Config config = ConfigUtils.loadConfig(configPath);
//        this.network = NetworkUtils.createNetwork(config);
        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputNetwork);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        this.network = scenario.getNetwork();
    }

    public void setCalcFreeways(boolean value){
        this.calcFreeways = value;
    }

    public void setFreewaySpeed(double speed){
        this.fwyCutOffSpeed = speed;
    }

    // Handlers are defined in the order that a trip should occur: activity end, enter traffic, enter link ... activity
    // start
    @Override
    public void handleEvent(ActivityEndEvent event) {
        //TODO move this checking activity type to the Commuter class
        if (Objects.equals(event.getActType(), this.home) || Objects.equals(event.getActType(), this.work)){
            // Add person if not already in the commuters containers
            if (!this.commuters.keySet().contains(event.getPersonId())){
                Commuter cmtr = new Commuter(this.network, this.home, this.work, this.fwyCutOffSpeed,
                        this.congestionCutOffSpeed);
                this.commuters.put(event.getPersonId(), cmtr);
            }
            // Create a new appropriate commute trip
            Commuter cmtr = this.commuters.get(event.getPersonId());
            Coord coord = this.network.getLinks().get(event.getLinkId()).getCoord();
            cmtr.activityEnds(event);
        }
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        if (!this.commuters.keySet().contains(event.getPersonId())) {
            // person not being tracked yet, so we can ignore event
            return;
        }
        Commuter cmtr = this.commuters.get(event.getPersonId());
        if (cmtr.trackingTrip){
            // tracked trip has just started
            cmtr.entersTraffic(event);
            // Update veh 2 person id map
            this.vehId2PersonId.put(event.getVehicleId(), event.getPersonId());
        }
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (!this.commuters.keySet().contains(this.vehId2PersonId.get(event.getVehicleId()))) {
            // person not being tracked yet, so we can ignore event
            return;
        }
        Commuter cmtr = this.commuters.get(this.vehId2PersonId.get(event.getVehicleId()));
        if (cmtr.trackingTrip) {
            // We are tracking this trip. Update the CommuteTrip.
            cmtr.entersLink(event);
        }
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if (!this.commuters.keySet().contains(this.vehId2PersonId.get(event.getVehicleId()))) {
            // person not being tracked yet, so we can ignore event
            return;
        }
        Commuter cmtr = this.commuters.get(this.vehId2PersonId.get(event.getVehicleId()));
        if (cmtr.trackingTrip){
            // We are tracking this trip. Update the CommuteTrip.
            cmtr.leavesLink(event);
        }
    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        if (!this.commuters.keySet().contains(this.vehId2PersonId.get(event.getVehicleId()))) {
            // person not being tracked yet, so we can ignore event
            return;
        }
        Commuter cmtr = this.commuters.get(this.vehId2PersonId.get(event.getVehicleId()));
        if (cmtr.trackingTrip){
            // We are tracking this trip. Update the CommuteTrip.
            cmtr.leavesTraffic(event);
        }
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (!this.commuters.keySet().contains(event.getPersonId())){
            // person not being tracked yet, so we can ignore event
            return;
        }
        Commuter cmtr = this.commuters.get(event.getPersonId());
        if (cmtr.trackingTrip){
            // We are tracking this trip. Update the CommuteTrip.
            cmtr.startActivity(event);
        }
    }


    @Override
    public void reset(int iteration) {
    }

    public void setHome(String home){
        this.home = home;
    }

    public void setWork(String work){
        this.work = work;
    }

    public void setCongestionCutOffSpeed(double speed){
        this.congestionCutOffSpeed = speed;
    }

    public HashMap<Id<Person>, Commuter> getCommuters(){
        return this.commuters;
    }
}
