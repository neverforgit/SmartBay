package input.counts;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import rx.Observable;
import rx.Single;

import java.util.Random;

import static com.github.davidmoten.rtree.geometry.Geometries.line;
import static com.github.davidmoten.rtree.geometry.Geometries.point;


/**
 * Created by Andrew A. Campbell on 1/24/18.
 */
public class RTreeTestRandomLines {
	public RTree<String, Line> tree = RTree.create();
	public Observable<Entry<String, Line>> entries;

	public void build(){

		Integer i = 0;
		Random r = new Random();
		while(i < 100){
			float xFrom = r.nextFloat()*125;
			float yFrom = r.nextFloat()*125;
			float xTo = r.nextFloat()*125;
			float yTo = r.nextFloat()*125;
			tree = tree.add(i.toString(), line(xFrom, yFrom, xTo, yTo));
			i++;
		}

		this.entries =
				tree.search(point(20,20), 5);



		Observable<Entry<String, Line>> nearest =	tree.nearest(point(20,20), 5.0, 10);
		Iterable<Entry<String, Line>> itr =  nearest.toBlocking().toIterable();
		Single<Entry<String, Line>> single = nearest.toSingle();
		for (Entry<String, Line> e : itr){
			System.out.println(e);
			System.out.println("1");
		}


//		List<Entry<String, Point>> single = this.entries.toList().toBlocking().single();

		tree.visualize(600,600)
				.save("/Users/daddy30000/rtree_viz.png");

		System.out.println("done");
	}

	public static void main(String[] args) {
		RTreeTestRandomLines rT = new RTreeTestRandomLines();
		rT.build();
	}

}

