package analysis.routing;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.router.TripRouter;
import org.matsim.core.scoring.ExperiencedPlansService;
import org.matsim.facilities.ActivityFacilitiesFactoryImpl;
import org.matsim.facilities.ActivityFacility;
import utils.CSVWriters;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

//import org.matsim.core.population.LegImpl;
//import org.matsim.core.population.PlanImpl;

/**
 * Created by Andrew A. Campbell on 6/6/16.
 *
 * Based on playground.vsp.energy.trafficstate.TrafficStateControlerListener
 *
 * Tests the TripRouter from the most recent mobsim to estimate routes for all the experienced routes in the
 * ExperiencedPlansImpl
 */
public class TripRouterListener implements IterationEndsListener {

    private int numAgents;
    private String outRootDir;
    private Scenario scenario;
    private TripRouter tripRouter;
    private int validateInterval;

    /**
     *
     * @param outRootDir Path to the root output directory where all MATSim output files are written.
     * @param numAgents Number of agents to validate routing for.
     */
    @Inject
    public TripRouterListener(String outRootDir, int numAgents, int validateInterval){
        this.outRootDir = outRootDir;
        this.numAgents = numAgents;
        this.validateInterval = validateInterval;
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event){
        // Only validate during the appropriate iterations
        if ((event.getIteration() % this.validateInterval) != 0){
            return; // do nothing
        }
        ////
        // Get the TripRouter created by the run
        ////
        System.out.println("////////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("////////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("TripRouterListener Validation");

        this.tripRouter = event.getServices().getInjector().getProvider(TripRouter.class).get();

        ////
        // Get the ExperiencedPlansService
        ////
        ExperiencedPlansService expPS = event.getServices().getInjector().getProvider(ExperiencedPlansService.class).get();
        Map<Id<Person>, Plan> experiencedPlans = expPS.getExperiencedPlans();

//        Map<Id<Person>, Plan> experiencedPlans = expPS.getAgentRecords();

        Long totalStartTime = System.currentTimeMillis();

        ArrayList<ArrayList<String>> values = new ArrayList<>();

        ////
        // Get actual and estimated routes
        ////
        ArrayList<String> personIds = new ArrayList<>();
        ArrayList<String> fromLinks = new ArrayList<>();
        ArrayList<String> toLinks = new ArrayList<>();
        ArrayList<String> depTimes = new ArrayList<>();
        ArrayList<String> estimTravelTimes = new ArrayList<>(); //Estimated leg travel times
        ArrayList<String> actualTravelTimes = new ArrayList<>();
        ArrayList<String> estimDistances = new ArrayList<>();
        ArrayList<String> actualDistances = new ArrayList<>();
        ArrayList<String> estimRoutes = new ArrayList<String>();
        ArrayList<String> actualRoutes = new ArrayList<String>();
        ArrayList<String> runTimes = new ArrayList<>();
//        Population runPop = this.scenario.getPopulation();
        ArrayList<RouteInformation> routeInfos = new ArrayList<RouteInformation>();
        double tStart; // start time for run-time recording
        double runTime;

        // Random sample of numAgents
        List<Id<Person>> allIds = new ArrayList<>(experiencedPlans.keySet());
        Collections.shuffle(allIds, new Random(999));
        ArrayList<Id<Person>> idSample = new ArrayList<>(allIds.subList(0, numAgents));

        for (Id<Person> id : idSample) {
            Plan plan = experiencedPlans.get(id);
            for (PlanElement pe : plan.getPlanElements())
                if (pe instanceof Leg) {
                    Leg leg = (Leg) pe;
                    if (leg.getRoute().getRouteType() == "links") {  // Some routes are "generic" and do not have links.
                        personIds.add(id.toString());
                        Id<Link> startLinkId = leg.getRoute().getStartLinkId();
                        fromLinks.add(startLinkId.toString());
                        Id<Link> endLinkId = leg.getRoute().getEndLinkId();
                        toLinks.add(endLinkId.toString());
                        Double depTime = leg.getDepartureTime();
                        depTimes.add(depTime.toString());
                        // Get actual values from the plans
                        Double legTT = leg.getTravelTime();
                        actualTravelTimes.add(legTT.toString());
                        ArrayList<Id<Link>> theRoute = new ArrayList<>();
                        theRoute.addAll(((LinkNetworkRouteImpl) leg.getRoute()).getLinkIds());
                        actualRoutes.add(this.stringFromRoute(theRoute));
                        actualDistances.add(String.valueOf(leg.getRoute().getDistance()));
                        // (NOT IN VERSION V1_2) Use PeviTripRouter.getInformation to estimate the route
                        // What we actually do is use the native MATSim class TripRouter to calculate the route
                        ActivityFacilitiesFactoryImpl actFact = new ActivityFacilitiesFactoryImpl();
                        ActivityFacility startFac =  actFact.createActivityFacility(
                                Id.create(0, ActivityFacility.class), startLinkId);
                        ActivityFacility endFac = actFact.createActivityFacility(
                                Id.create(1, ActivityFacility.class), endLinkId);
                        tStart = System.currentTimeMillis();
                        Person person = PopulationUtils.getFactory().createPerson(Id.create(1, Person.class));
                        List<? extends PlanElement> tmpRoute = this.tripRouter.calcRoute("car", startFac, endFac,
                                depTime, person);
                        runTime = System.currentTimeMillis() - tStart;
                        runTimes.add(String.valueOf(runTime));
                        Leg estimLeg = (Leg) tmpRoute.get(0);
                        LinkNetworkRouteImpl estimRoute = (LinkNetworkRouteImpl) estimLeg.getRoute();
                        estimRoutes.add(this.stringFromRoute(estimRoute.getLinkIds()));
                        estimTravelTimes.add(String.valueOf(estimLeg.getTravelTime()));
                        estimDistances.add(String.valueOf(estimRoute.getDistance()));
                    }
                }

        }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Write the actual and estimated values to a csv
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        values.add(personIds);
        values.add(fromLinks);
        values.add(toLinks);
        values.add(depTimes);
        values.add(actualTravelTimes);
        values.add(actualDistances);
        values.add(actualRoutes);
        values.add(estimTravelTimes);
        values.add(estimDistances);
        values.add(estimRoutes);
        values.add(runTimes);

        ArrayList<String> header = new ArrayList<>();
        header.add("Id");
        header.add("fromLink");
        header.add("toLink");
        header.add("depTime");
        header.add("actualTravelTime");
        header.add("actualDistance");
        header.add("actualRoute");
        header.add("estimTravelTime");
        header.add("estimDistance");
        header.add("estimRoute");
        header.add("runTime_[milli_sec]");

        // Write the output
        String lastIter = String.valueOf(event.getIteration());
        String runId = event.getServices().getConfig().controler().getRunId();
        String outPath = MessageFormat.format("{0}/ITERS/it.{1}/{2}.{1}.ValidationTripRouter.txt", outRootDir,
                lastIter, runId);
        try {
            CSVWriters.writeFileJDK7(values, header, ',' ,outPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total Running Time [sec]: " + (System.currentTimeMillis() - totalStartTime)/1000.0 + "\n");
    }

    private String stringFromRoute(List<Id<Link>> route){
        String out = "[";
        for (Id<Link> id : route){
            out += String.valueOf(id) + " ";
        }
        out += "]";
        return out;
    }
}
