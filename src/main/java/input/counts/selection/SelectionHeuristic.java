package input.counts.selection;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import rx.Observable;

/**
 * Created by Andrew A. Campbell on 1/29/18.
 */
public interface SelectionHeuristic {

	/**
	 * Takes the links returned by a call to RTree (tree.search or tree.nearest) and applies
	 * a heuristic to return the best (or none) link.
	 * @param links Observable returned by call to RTree search or nearest methods.
	 * @param point A point to be matched to one (or none) of the links.
	 * @return An Object that identifies the selected link. Of the same type as the Object keys (or values in the RTree
	 * parlance) for the input Observable.
	 */
	public Object selectLink(Observable<Entry<Long, Line>> links, Point point);

}
