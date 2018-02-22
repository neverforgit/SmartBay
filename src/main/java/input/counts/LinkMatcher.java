package input.counts;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.selectors.LinkSelector;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Andrew A. Campbell on 1/29/18.
 */
public class LinkMatcher {

	private OsmRTree osmRTree;
	private LinkSelector linkSelector;
	private HashMap<Object, Point> locations;
	HashMap<Object, Long> matched;
	private HashMap<Object, ArrayList<String>> notMatched = new HashMap<>();

	public double maxSearchDist = 0.01;
	public int kNearest = 20;  // number of road segments to be returned when searching k-nearest

	/**
	 *
	 * @param osmRTree
	 * @param selector
	 * @param locations Keys are generic (i.e. string, int, float etc.). The value is an x-y array (in that order).
	 */
	public LinkMatcher(OsmRTree osmRTree, LinkSelector selector, HashMap<Object, Point> locations){
		this.osmRTree = osmRTree;
		this.linkSelector = selector;
		this.locations = locations;
	}

	public HashMap<Object, Long> matchPointsToLinks(){
		HashMap<Object, Long> out = new HashMap<>();
		for (Object k : this.locations.keySet()){
			Point p = this.locations.get(k);
			Observable<Entry<Long, Line>> nearestLinks = osmRTree.tree.nearest(p, this.maxSearchDist, this.kNearest);
			if (nearestLinks.isEmpty().toBlocking().single()){  // no links close enough
				ArrayList<String> s = new ArrayList<>(Arrays.asList("DISTANCE"));
				this.notMatched.put(k, s);
				continue;
			} else {  // apply selection heuristics
				Long wayId = (Long) this.linkSelector.selectLink(nearestLinks, k, p);
				if (wayId != null) {
					int i = 1;
					out.put(k, wayId);
				}
			}
		}
		this.matched = out;
		this.notMatched.putAll(this.linkSelector.getNotMachedLinks());
		return out;
	}

	public HashMap<Object, ArrayList<String>> getNotMatched() {
		return notMatched;
	}
}
