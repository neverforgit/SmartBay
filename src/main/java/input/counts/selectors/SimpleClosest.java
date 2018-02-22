package input.counts.selectors;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;

import static input.counts.selectors.utils.argMin;
import static input.counts.selectors.utils.distToLineSegment;

/**
 * Created by Andrew A. Campbell on 1/29/18.
 */
public class SimpleClosest implements LinkSelector {
	@Override
	public Long selectLink(Observable<Entry<Long, Line>> links, Object pointID, Point point) {
		Iterable itr = links.toBlocking().toIterable();
		int sze = links.count().toBlocking().single();
		double[] dists = new double[sze];
		Long[] vals = new Long[sze];
		int i = 0;
		for (Entry<Long, Line> e : links.toBlocking().toIterable()) {
			Line l = e.geometry();
			dists[i] = distToLineSegment(point, l);
			vals[i] = e.value();
			i++;
		}
		int idx = argMin(dists);  // index of closest link
		return vals[idx];
	}

	@Override
	public HashMap<Object, ArrayList<String>> getNotMachedLinks() {
		return null;
	}
}
