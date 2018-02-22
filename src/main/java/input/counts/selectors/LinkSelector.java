package input.counts.selectors;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andrew A. Campbell on 1/29/18.
 */
public interface LinkSelector {

	/**
	 * Takes the links returned by a call to RTree (tree.search or tree.nearest) and applies
	 * a heuristic to return the best (or none) link.
	 * @param links Observable returned by call to RTree search or nearest methods.
	 * @param pointID Unique indentifier for point
	 * @param point A point to be matched to one (or none) of the links.
	 * @return An Object that identifies the selected link. Of the same type as the Object keys (or values in the RTree
	 * parlance) for the input Observable.
	 */
	public Object selectLink(Observable<Entry<Long, Line>> links, Object pointID, Point point);

	/**
	 * A map of the links that could not be matched and string code explaining why.
	 * @return
	 */
	public HashMap<Object, ArrayList<String>> getNotMachedLinks();

}
