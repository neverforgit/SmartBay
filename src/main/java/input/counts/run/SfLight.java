package input.counts.run;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.SensorsToWaysMatcher;
import input.counts.OsmParser;
import input.counts.OsmRTree;
import input.counts.selectors.LinkSelector;
import input.counts.selectors.SimpleClosest;

import java.io.*;
import java.util.HashMap;

import static com.github.davidmoten.rtree.geometry.Geometries.point;

/**
 * Runs the parsing and link matching on a large test area clipped from the SF Bay Area region
 *
 * Created by Andrew A. Campbell on 1/31/18.
 */
public class SfLight {

	public static void main(String[] args) {
		String osmPath = args[0];
		String rtreeVizPath = args[1];
		String countsLocations = args[2];
		String matchedOut = args[3];  // output file to write matching results

		System.out.println("Running OsmParser");
		OsmParser osmP = new OsmParser(osmPath);

		System.out.println("Testing OsmRTree");
		OsmRTree osmTree = new OsmRTree(osmP.getOsm());
		osmTree.makeRTree();
		RTree<Long, Line> tree = osmTree.tree;
		tree.visualize(600, 600)
				.save(rtreeVizPath);
		System.out.println("DONE BUILDING RTREE");
		System.out.println();


		System.out.println("link-matching with SimpleClosest selection heuristic");
		// Add all the sensor locations
		HashMap<Object, Point> locations = new HashMap<>();
		BufferedReader br = null;
		String line;
		try {
			br = new BufferedReader(new FileReader(countsLocations));
			br.readLine();  // burn header
			while ((line = br.readLine()) != null){
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
		LinkSelector selector = new SimpleClosest();
		SensorsToWaysMatcher matcher = new SensorsToWaysMatcher(osmTree, selector, locations);
		HashMap<Object, Long> matches = matcher.matchPointsToLinks();
		for (Object k : matches.keySet()){
			System.out.println("Point, " + k + ", matched to line: " + matches.get(k));
		}

		// Write the output
		BufferedWriter bW = null;
		try {
			bW = new BufferedWriter(new FileWriter(matchedOut));
			bW.write("PeMS_ID,OSM_ID,x,y\n");
			String s;
			for (Object k : matches.keySet()){
				s = k + "," + matches.get(k) + "," + locations.get(k	).x() + "," + locations.get(k).y() + "\n";
				bW.write(s);
			}
			bW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("NUMBER OF MATCHES: " + matches.size());
	}
}
