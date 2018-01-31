package input.counts;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

import static com.github.davidmoten.rtree.geometry.Geometries.line;
import static com.github.davidmoten.rtree.geometry.Geometries.point;


/**
 * Created by Andrew A. Campbell on 1/24/18.
 */
public class RTreeTestToyNetwork {
	public RTree<String, Line> tree = RTree.create();
//	public Observable<Entry<String, Line>> entries;

	public void build(){


		//Build the square network
		tree = tree.add("a", line(3, 3, 3, 7))
				.add("b", line(3, 7, 7, 7))
				.add("c", line(7, 7, 7, 3))
				.add("d", line(7, 3, 3, 3));

		// Search for some points
		List<Point> points = new ArrayList<>();
		points.add(point(5,3));
		points.add(point(8, 5));
		points.add(point(5, 5));

		Integer i = 0;
		for (Point p : points){
			Observable<Entry<String, Line>> entries = tree.nearest(p, 5, 4);
			System.out.println("Iteration: " + i.toString());
			System.out.println("Point (x,y): (" + String.valueOf(p.x()) + ", " + String.valueOf(p.y()) + ")");
			for (Entry e : entries.toBlocking().toIterable()){
				System.out.println(e);
			}
			System.out.println();
			i++;
		}
		tree.visualize(600,600)
				.save("/Users/daddy30000/rtree_viz.png");

		System.out.println("done");
	}

	public static void main(String[] args) {
		RTreeTestToyNetwork rT = new RTreeTestToyNetwork();
		rT.build();
	}

}

