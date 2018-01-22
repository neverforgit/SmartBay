package analysis.routing;

import org.matsim.api.core.v01.population.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew A. Campbell on 4/28/16.
 */
public class RouteInformation {
    private ArrayList<RouteInformationElement> routeElements = new ArrayList<RouteInformationElement>();
    private Route route;

    public  RouteInformation(Route rt){
        this.route = rt;
    }

    public void addRouteInformationElement(RouteInformationElement re){
        this.routeElements.add(re);
    }

    public List<RouteInformationElement> getRouteElements(){
        try {
            return this.routeElements;
        } catch (NullPointerException e) {
            System.out.println("No RoutInformationElements have been added yet.");
        }
        return this.routeElements;
    }

    public Route getRoute(){
        return this.route;
    }



}
