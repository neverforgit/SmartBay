package input.counts.run;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.LinkMatcher;
import input.counts.OsmParser;
import input.counts.OsmRTree;
import input.counts.selectors.LinkSelector;
import input.counts.selectors.SimpleClosest;

import java.util.HashMap;

import static com.github.davidmoten.rtree.geometry.Geometries.point;

/**
 * Created by Andrew A. Campbell on 1/29/18.
 */
public class TestParsingAndMatching {

	public static void main(String[] args) {
		String osmPath = args[0];
		String rtreeVizPath = args[1];

		System.out.println("Testing OsmParser");
		OsmParser osmP = new OsmParser(osmPath);


		System.out.println("Testing OsmRTree");
		OsmRTree osmTree = new OsmRTree(osmP.getOsm());

		osmTree.includeHighways.add("trunk");
		osmTree.makeRTree();
		RTree<Long, Line> tree = osmTree.tree;
		tree.visualize(600, 600)
				.save(rtreeVizPath);
		System.out.println("DONE BUILDING RTREE");
		System.out.println();


		System.out.println("Testing link-matching with SimpleClosest selection heuristic");
		LinkSelector selector = new SimpleClosest();
		HashMap<Object, Point> locations = new HashMap<>();
		locations.put("a", point(0.005, 0.029));
		locations.put("b", point(0.007,0.029));
		locations.put("c", point(0.002,0.015));
		LinkMatcher matcher = new LinkMatcher(osmTree, selector, locations);
		HashMap<Object, Long> matches = matcher.matchPointsToLinks();
		for (Object k : matches.keySet()){
			System.out.println("Point, " + k + ", matched to line: " + matches.get(k));
		}

	}
}
