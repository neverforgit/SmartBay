package input.counts;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Point;
import rx.Observable;
import rx.Single;

import java.util.Random;

import static com.github.davidmoten.rtree.geometry.Geometries.point;




/**
 * Created by Andrew A. Campbell on 1/24/18.
 */
public class RTreeTestPoints {
	public RTree<String, Point> tree = RTree.create();
	public Observable<Entry<String, Point>> entries;

	public void build(){
		tree = tree.add("DAVE", point(10, 20))
				.add("FRED", point(12, 25))
				.add("MARY", point(97, 125));

		Integer i = 0;
		Random r = new Random();
		while(i < 1000){
			float x = r.nextFloat()*125;
			float y = r.nextFloat()*125;
			tree = tree.add(i.toString(), point(x, y));
			i++;
		}

		this.entries =
				tree.search(point(20,20), 5);

		Observable<Entry<String, Point>> nearest =	tree.nearest(point(20,20), 5.0, 10);
		Iterable<Entry<String, Point>> itr =  nearest.toBlocking().toIterable();
		Single<Entry<String, Point>> single = nearest.toSingle();
		for (Entry<String, Point> e : itr){
			System.out.println(e);
			System.out.println("1");
		}


//		List<Entry<String, Point>> single = this.entries.toList().toBlocking().single();

		tree.visualize(600,600)
				.save("/Users/daddy30000/rtree_viz.png");

		System.out.println("done");
	}

	public static void main(String[] args) {
		RTreeTestPoints rT = new RTreeTestPoints();
		rT.build();
	}

}

