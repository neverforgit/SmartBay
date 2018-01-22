package analysis.routing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

/**
 * Created by Andrew A. Campbell on 4/28/16.
 */
public class RouteInformationElement {
    private Id<Link> linkId; // sequential list of links on route
    private Double linkTravelTime;
    private Double linkDistance;
    private Double linkSpeed;

    public RouteInformationElement(){}

    public void addLinkId(Id<Link> id){
        this.linkId = id;
    }

    public void addLinkTravelTime(double tt){
        this.linkTravelTime = tt;
    }

    public void addlinkDistance(double dist){
        this.linkDistance = dist;
    }

    public void addLinkSpeed(double speed){
        this.linkSpeed = speed;
    }

    public Id<Link> getLinkId(){
        return this.linkId;
    }

    public Double getLinkTravelTime(){
        return this.linkTravelTime;
    }

    public Double getLinkDistance(){
        return this.linkDistance;
    }

    public Double getLinkSpeed(){
        return this.linkSpeed;
    }

    public void printRouteElement(){
        System.out.println("Link Id: " + this.getLinkId());
        System.out.println("Link Travel Time: " + this.getLinkTravelTime());
        System.out.println("Link Distance: " + this.getLinkDistance());
        System.out.println("Link Speed: " + this.getLinkSpeed());
    }



}
