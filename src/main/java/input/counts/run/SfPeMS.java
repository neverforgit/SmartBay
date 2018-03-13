package input.counts.run;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.SensorsToWaysMatcher;
import input.counts.OsmParser;
import input.counts.OsmRTree;
import input.counts.selectors.LinkSelector;
import input.counts.selectors.PeMSLinkSelector;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static com.github.davidmoten.rtree.geometry.Geometries.point;

/**
 * Runs the parsing and link matching on a large test area clipped from the SF Bay Area region
 *
 * Created by Andrew A. Campbell on 1/31/18.
 */
public class SfPeMS {

	public static void main(String[] args) {
		Long t0 = System.currentTimeMillis();

		String osmPath = args[0];
		String rtreeVizPath = args[1];
		String filteredCountsLocations = args[2];
		String pemsMetaData = args[3];
		String matchedOut = args[4];  // output file to write matching results
		String notMatchedOut = args[5]; // path to output of log of not-matched links

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


		System.out.println("link-matching with PeMSLinkSelector_V2 selection heuristic");
		// Add all the sensor locations
		HashMap<Object, Point> locations = new HashMap<>();
		BufferedReader br = null;
		String line;
		try {
			br = new BufferedReader(new FileReader(filteredCountsLocations));
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
		LinkSelector pLS = new PeMSLinkSelector(osmP.getOsm(), pemsMetaData);
		SensorsToWaysMatcher matcher = new SensorsToWaysMatcher(osmTree, pLS, locations);
		matcher.kNearest = 50;
		HashMap<Object, Long> matches = matcher.matchPointsToLinks();  // keys are sensor ids, vals are OSM way IDs
		for (Object k : matches.keySet()){
			System.out.println("Point, " + k + ", matched to line: " + matches.get(k));
		}

		// Write the matched output
		BufferedWriter bW = null;
		try {
			bW = new BufferedWriter(new FileWriter(matchedOut));
			bW.write("PeMS_ID,OSM_ID,x,y\n");
			String s;
			for (Object k : matches.keySet()){
				s = k + "," + matches.get(k) + "," + locations.get(k).x() + "," + locations.get(k).y() + "\n";
				bW.write(s);
			}
			bW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Write the not matched output
		HashMap<Object, ArrayList<String>> notMatched = matcher.getNotMatched();
		try {
			bW = new BufferedWriter(new FileWriter(notMatchedOut));
			bW.write("PeMS_ID,Reasons\n");
			String s;
			for (Object k : notMatched.keySet()){
				s = k + "," +  notMatched.get(k).toString() + "\n";
				bW.write(s);
			}
			bW.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		System.out.println("NUMBER OF MATCHES: " + matches.size());
		Long tt = System.currentTimeMillis() - t0;
		System.out.println("Total running time [mSec]: " + tt);
	}
}
