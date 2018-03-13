package input.counts.run;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.OsmParser;
import input.counts.OsmRTree;
import input.counts.SensorToLinkMatcher;
import input.counts.SensorsToWaysMatcher;
import input.counts.selectors.LinkSelector;
import input.counts.selectors.PeMSLinkSelector;
import input.network.NewOsmNetworkReader;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.*;
import java.util.HashMap;

import static com.github.davidmoten.rtree.geometry.Geometries.point;

/**
 * Runs the parsing and link matching on a large test area clipped from the SF Bay Area region
 *
 * Created by Andrew A. Campbell on 1/31/18.
 */
public class SfPeMSAll {

	public static void main(String[] args) {
		Long t0 = System.currentTimeMillis();

		String osmPath = args[0];
		String filteredCountsLocations = args[1];
		String pemsMetaData = args[2];
		String matNetPath = args[3];
		String sensors2LinksPath = args[4];

		////
		// #1 - OsmParser
		////
		System.out.println("Running OsmParser");
		OsmParser osmP = new OsmParser(osmPath);

		////
		// #2 - OsmTree
		////
		OsmRTree osmTree = new OsmRTree(osmP.getOsm());
		osmTree.makeRTree();
		RTree<Long, Line> tree = osmTree.tree;

		////
		// #3 - Map sensor ids to OSM Ways (SensorsToWaysMatcher)
		////

		// Add all the sensor locations
		HashMap<Object, Point> locations = new HashMap<>();
		BufferedReader br = null;
		String line;
		try {
			br = new BufferedReader(new FileReader(filteredCountsLocations));
			br.readLine();  // burn header
			while ((line = br.readLine()) != null) {
				String[] elements = line.split(",");
				locations.put(elements[0], point(Double.valueOf(elements[2]), Double.valueOf(elements[1])));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Now conduct the matching
		LinkSelector pLS = new PeMSLinkSelector(osmP.getOsm(), pemsMetaData);
		SensorsToWaysMatcher matcher = new SensorsToWaysMatcher(osmTree, pLS, locations);
		matcher.kNearest = 50;
		HashMap<Object, Long> sensors2Ways = matcher.matchPointsToLinks();  // keys are sensor ids, vals are OSM way IDs

		////
		// #4 - NewOsmNetworkReader
		///
		CoordinateTransformation coordTrans = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:26910");
		Network net = NetworkUtils.createNetwork();
		NewOsmNetworkReader nONR = new NewOsmNetworkReader(net, coordTrans);
		nONR.parse(osmPath);
		new NetworkCleaner().run(net);
		new NetworkWriter(net).write(matNetPath);

		////
		// #5 - Match Sensors to Links and write to csv
		////
		HashMap<Long, Id<Link>> ways2Links = new HashMap<Long, Id<Link>>(nONR.getWays2Links());
		SensorToLinkMatcher sTLM = new SensorToLinkMatcher(sensors2Ways, ways2Links, net);
		sTLM.matchSensorsToLinks();
		HashMap<Object, Id<Link>> sensors2Links = sTLM.getSensorsToLinks();
		try {
			BufferedWriter bW = new BufferedWriter(new FileWriter(sensors2LinksPath));
			bW.write("ID,Link\n");
			for (Object id : sensors2Links.keySet()){
				bW.write(id + "," + sensors2Links.get(id) + "\n");
			}
			bW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
