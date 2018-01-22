package utils;

import com.google.common.collect.Lists;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.util.CSVReaders;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.counts.CountSimComparison;
import org.matsim.counts.CountSimComparisonImpl;
import org.matsim.counts.algorithms.CountSimComparisonKMLWriter;
import org.matsim.counts.algorithms.CountsHtmlAndGraphsWriter;
import org.matsim.counts.algorithms.graphs.CountsErrorGraphCreator;
import org.matsim.counts.algorithms.graphs.CountsLoadCurveGraphCreator;
import org.matsim.counts.algorithms.graphs.CountsSimReal24GraphCreator;
import org.matsim.counts.algorithms.graphs.CountsSimRealPerHourGraphCreator;

import java.text.MessageFormat;
import java.util.List;


/**
 * Copied from sandbox.sfwatergit.utilities.count_parser;
 *
 * Parses counts file. Writes to KML.
 *
 * TODO: Permit writing of multiple counts files.
 * Created by sidneyfeygin on 5/23/16.
 * This is Andrew's modified version. This version is designed to be run on standard MATSim output directory. It infers
 * paths based on the config and root output.
 *
 * System input:
 * 0 - Path to config
 * 1 - MATSim output root directory
 * 2 - Iteration to use for generating validation files.
 */
public class MatsimCountParser {

    public static void writeKML(List<CountSimComparison> comparisons, Network network, String outfile){
        CountSimComparisonKMLWriter countSimComparisonKMLWriter = new CountSimComparisonKMLWriter(comparisons,network, TransformationFactory.getCoordinateTransformation("epsg:26910","epsg:4326"));
        countSimComparisonKMLWriter.writeFile(outfile);
        System.out.println("Done writing KML!");
    }

    public static void writeGraphs(String graphsBaseDir, List<CountSimComparison> comparisons, int iteration){
        CountsHtmlAndGraphsWriter cgw = new CountsHtmlAndGraphsWriter(graphsBaseDir, comparisons, iteration);
        cgw.addGraphsCreator(new CountsSimRealPerHourGraphCreator("sim and real volumes"));
        cgw.addGraphsCreator(new CountsErrorGraphCreator("errors"));
        cgw.addGraphsCreator(new CountsLoadCurveGraphCreator("link volumes"));
        cgw.addGraphsCreator(new CountsSimReal24GraphCreator("average working day sim and count volumes"));
        cgw.createHtmlAndGraphs();
    }

    public static List<CountSimComparison> parseFile(String filename){
        final List<String[]> strings = readFile(filename);
        if (strings != null) {
            strings.remove(0);  // remove header
        }
        List<CountSimComparison> comparisons = Lists.newArrayList();
        assert strings != null;
        for (String[] row : strings) {
            final Id<Link> linkId = Id.createLinkId(row[0]);
            int hour = Integer.parseInt(row[1]);
            double countsValSim = Double.parseDouble(row[2]);
            double countsVal2 = Double.parseDouble(row[3]);
            comparisons.add(new CountSimComparisonImpl(linkId,hour,countsVal2,countsValSim));
        }
        return comparisons;
    }

    private static List<String[]> readFile(String filename){
        return CSVReaders.readTSV(filename);

    }

    /**
     * Sample main call. Will generate the kml to output file according to provided arguments.
     *
     * @param args 0: config path
     *             1: ouput directory
     *             2: iteration to generate figures for
     */
    public static void main(String[] args) {
        final Network network = ScenarioUtils.loadScenario(ConfigUtils.loadConfig(args[0])).getNetwork();
        String countsPath = MessageFormat.format("{0}/ITERS/it.{1}/run0.{1}.countscompare_rescaled.txt", args[1], args[2]);
        List<CountSimComparison> comparisons = MatsimCountParser.parseFile(countsPath);
        String kmlPath = MessageFormat.format("{0}/ITERS/it.{1}/run0.{1}.countscompare_rescaled.kmz", args[1], args[2]);
        MatsimCountParser.writeKML(comparisons,network,kmlPath);
        String graphParentDir = MessageFormat.format("{0}/ITERS/it.{1}", args[1], args[2]);
        MatsimCountParser.writeGraphs(graphParentDir, comparisons, Integer.valueOf(args[2]));
    }
}