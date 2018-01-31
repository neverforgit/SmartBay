package input.counts;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.selection.SelectionHeuristic;
import input.counts.selection.SimpleClosest;

import java.util.HashMap;

import static com.github.davidmoten.rtree.geometry.Geometries.point;

/**
 * Created by Andrew A. Campbell on 1/29/18.
 */
public class TestParsingAndMatching {

	public static void main(String[] args) {
		String osmPath = args[0];
		String rtreeVizPath = args[1];
		String countsPath = args[2];

		System.out.println("Testing OsmParser");
		OsmParser osmP = new OsmParser(osmPath);
		osmP.includeHighways.add("trunk");
		osmP.makeRTree();
		RTree<Long, Line> tree = osmP.tree;
		tree.visualize(600, 600)
				.save(rtreeVizPath);
		System.out.println("DONE BUILDING RTREE");
		System.out.println();

		System.out.println("Testing link-matching with SimpleClosest selection heuristic");
		SelectionHeuristic selector = new SimpleClosest();
		HashMap<Object, Point> locations = new HashMap<>();
		locations.put("a", point(0.005, 0.029));
		locations.put("b", point(0.007,0.029));
		locations.put("c", point(0.002,0.015));
		LinkMatcher matcher = new LinkMatcher(osmP, selector, locations);
		HashMap<Object, Long> matches = matcher.matchPointsToLinks();
		for (Object k : matches.keySet()){
			System.out.println("Point, " + k + ", matched to line: " + matches.get(k));
		}

	}
}
