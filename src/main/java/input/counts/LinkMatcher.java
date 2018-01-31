package input.counts;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.selection.SelectionHeuristic;
import rx.Observable;

import java.util.HashMap;

/**
 * Created by Andrew A. Campbell on 1/29/18.
 */
public class LinkMatcher {

	private OsmParser osmParser;
	private SelectionHeuristic selectionHeuristic;
	private HashMap<Object, Point> locations;

	public double maxSearchDist = 0.01;
	public int kNearest = 20;  // number of road segments to be returned when searching k-nearest

	/**
	 *
	 * @param osmParser
	 * @param selector
	 * @param locations Keys are generic (i.e. string, int, float etc.). The value is an x-y array (in that order).
	 */
	LinkMatcher(OsmParser osmParser, SelectionHeuristic selector, HashMap<Object, Point> locations){
		this.osmParser = osmParser;
		this.selectionHeuristic = selector;
		this.locations = locations;
	}

	public HashMap<Object, Long> matchPointsToLinks(){
		HashMap<Object, Long> out = new HashMap<>();
		for (Object k : this.locations.keySet()){
			Point p = this.locations.get(k);
			Observable<Entry<Long, Line>> nearestLinks = osmParser.tree.nearest(p, this.maxSearchDist, this.kNearest);
			if (nearestLinks.isEmpty().toBlocking().single()){
				break;
			}
			Long wayId = (Long) this.selectionHeuristic.selectLink(nearestLinks, p);
			out.put(k, wayId);
		}
		return out;
	}
}
