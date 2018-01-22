package analysis.commute;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.StringJoiner;

/**
 * Created by Andrew A. Campbell on 9/26/16.
 *
 * Parse the events log to get attributes of the commute trips.
 *
 */
public class CommuteAnalyzer {
    private final String eventsFile;
    private EventsManager eventsManager;
    private CommuteHandler commuteHandler;
    private String outTotalsName = "ValidationCommuteAnalyzerTotals.txt";
    private String outFreewayName = "ValidationCommuteAnalyzerFwy.txt";
    private boolean calcFreeway = true;


    /**
     * By default, we will also run calculations for freeway links separately from the all-links totals. The default
     * freeway cutoff speed is 33.3 meter/sec.
     * @param eventsFile
     * @param netPath Relative path to network file.
     */
    public CommuteAnalyzer(String eventsFile, String netPath) {
        this.eventsFile = eventsFile;
        this.commuteHandler = new CommuteHandler(netPath);
    }

    /**
     * Use this if you only want to calculate measures for whole commute trips instead of also doing separate
     * calculations for freeway links.
     */
    public void dontCalcFreeways(){
        this.commuteHandler.setCalcFreeways(false);
        this.calcFreeway = false;
    }

    public CommuteHandler getCommuteHandler(){
        return this.commuteHandler;
    }

    public void run(){
        this.eventsManager = EventsUtils.createEventsManager();
        MatsimEventsReader eventsReader = new MatsimEventsReader(this.eventsManager);
        this.eventsManager.addHandler(this.commuteHandler);
        eventsReader.readFile(this.eventsFile);
    }

    /**
     * Writes out all the CommuteTrip measures for each agent to a csv. If calc freeways enabled, writes a second file
     * with only the freeway trips.
     * @param outDir Path to the root output directory.
     */
    public void write(String outDir) throws IOException {
        ////
        // Write the trip totals file
        ////
        String outTotalsPath = Paths.get(outDir, this.outTotalsName).toString();
        BufferedWriter writer = IOUtils.getBufferedWriter(outTotalsPath);
        // build the header
        StringJoiner header = new StringJoiner("\t");
        header.add("AgentId");
        // Home-to-work
        header.add("HomeX");
        header.add("HomeY");
        header.add("HomeDepTime");
        header.add("TotalTimeH2W");
        header.add("DelayTimeH2W");
        header.add("TimeInCongestionH2W");
        header.add("TotalDistH2W");
        header.add("DelayDistH2W");
        header.add("DistInCongestionH2W");
        // Work-to-home
        header.add("WorkX");
        header.add("WorkY");
        header.add("WorkDepTime");
        header.add("TotalTimeW2H");
        header.add("DelayTimeW2H");
        header.add("TimeInCongestionW2H");
        header.add("TotalDistW2H");
        header.add("DelayDistW2H");
        header.add("DistInCongestionW2H");
        header.add("\n");
        // Write
        writer.write(header.toString());
        // Iterate through all Commuters and write their CommuteTrips
        HashMap<Id<Person>, Commuter> commuters = this.commuteHandler.getCommuters();
        for (Id<Person> id: commuters.keySet()){
            Commuter cmtr = commuters.get(id);
            // build the row
            StringJoiner row = new StringJoiner("\t");
            row.add(id.toString());
            // h2w trip
            HashMap<String, Coord> coords = cmtr.getHWCoords();
            boolean h2wObserved = true;
            try {
                row.add(String.valueOf(coords.get("home").getX()));
                row.add(String.valueOf(coords.get("home").getY()));
                HashMap<String, Double> h2wTrip = cmtr.getTripH2W().getAllAttributes();
                row.add(h2wTrip.get("startTime").toString());
                row.add(h2wTrip.get("totalTime").toString());
                row.add(String.valueOf(Math.round(h2wTrip.get("totalDelay"))));
                row.add(h2wTrip.get("congestedTime").toString());
                row.add(h2wTrip.get("totalDist").toString());
                row.add(h2wTrip.get("delayDist").toString());
                row.add(h2wTrip.get("congestedDist").toString());
            } catch (NullPointerException e){
//                System.out.println("No H2W trip for person " + id.toString());
                row.add("\t\t\t\t\t\t");
                h2wObserved = false;
            }
            // w2h trip
            boolean w2hObserved = true;
            try {
                row.add(String.valueOf(coords.get("work").getX()));
                row.add(String.valueOf(coords.get("work").getY()));
                HashMap<String, Double> w2hTrip = cmtr.getTripW2H().getAllAttributes();
                row.add(w2hTrip.get("startTime").toString());
                row.add(w2hTrip.get("totalTime").toString());
                row.add(String.valueOf(Math.round(w2hTrip.get("totalDelay"))));
                row.add(w2hTrip.get("congestedTime").toString());
                row.add(w2hTrip.get("totalDist").toString());
                row.add(w2hTrip.get("delayDist").toString());
                row.add(w2hTrip.get("congestedDist").toString());
                row.add("\n");
            } catch (NullPointerException e){
//                System.out.println("No W2H trip for person " + id.toString());
                row.add("\t\t\t\t\t\t\n");
                w2hObserved = false;
            }
            // Only write row if at least one commute trip observed.
            if (h2wObserved || w2hObserved) {
                writer.write(row.toString());
            }
        }
        writer.close();
        ////
        // Write the freeway file
        ////
        if (this.calcFreeway){
            String outFwyPath = Paths.get(outDir, this.outFreewayName).toString();
            BufferedWriter writerFwy = IOUtils.getBufferedWriter(outFwyPath);
            // Write
            writerFwy.write(header.toString());
            // Iterate through all Commuters and write their CommuteTrips
            for (Id<Person> id: commuters.keySet()){
                Commuter cmtr = commuters.get(id);
                // build the row
                StringJoiner row = new StringJoiner("\t");
                // h2w trip
                HashMap<String, Coord> coords = cmtr.getHWCoords();
                boolean h2wObserved = true;
                try {
                    row.add(id.toString());
                    row.add(String.valueOf(coords.get("home").getX()));
                    row.add(String.valueOf(coords.get("home").getY()));
                    HashMap<String, Double> h2wTrip = cmtr.getTripH2W().getFreewayAttributes();
                    row.add(h2wTrip.get("startTime").toString());
                    row.add(h2wTrip.get("totalTime").toString());
                    row.add(String.valueOf(Math.round(h2wTrip.get("totalDelay"))));
                    row.add(h2wTrip.get("congestedTime").toString());
                    row.add(h2wTrip.get("totalDist").toString());
                    row.add(h2wTrip.get("delayDist").toString());
                    row.add(h2wTrip.get("congestedDist").toString());
                } catch (NullPointerException e) {
//                    System.out.println("No Freeway H2W trip for person " + id.toString());
                    row.add("\t\t\t\t\t\t");
                    h2wObserved = false;
                }
                // w2h trip
                boolean w2hObserved = true;
                try {
                    row.add(String.valueOf(coords.get("work").getX()));
                    row.add(String.valueOf(coords.get("work").getY()));
                    HashMap<String, Double> w2hTrip = cmtr.getTripW2H().getFreewayAttributes();
                    row.add(w2hTrip.get("startTime").toString());
                    row.add(w2hTrip.get("totalTime").toString());
                    row.add(String.valueOf(Math.round(w2hTrip.get("totalDelay"))));
                    row.add(w2hTrip.get("congestedTime").toString());
                    row.add(w2hTrip.get("totalDist").toString());
                    row.add(w2hTrip.get("delayDist").toString());
                    row.add(w2hTrip.get("congestedDist").toString());
                    row.add("\n");
                } catch (NullPointerException e){
//                    System.out.println("No Freeway W2H trip for person " + id.toString());
                    row.add("\t\t\t\t\t\t\n");
                    w2hObserved = false;
                }
                // Only write row if at least one commute trip observed.
                if (h2wObserved || w2hObserved) {
                    writerFwy.write(row.toString());
                }
            }
            writerFwy.close();
        }
    }

    /**
     * Used to overwrite the default name for the output totals file.
     * @param name
     */
     public void setOutTotalsName(String name){
        this.outTotalsName = name;
    }

    /**
     * Used to overwrite the default name for the ouput freeways file.
     * @param name
     */
    public void setOutFreewayName(String name){
        this.outFreewayName = name;
    }

}