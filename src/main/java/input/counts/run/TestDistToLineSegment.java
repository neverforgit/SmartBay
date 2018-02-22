package input.counts.run;

import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import input.counts.selectors.SimpleClosest;

import java.util.Arrays;

import static com.github.davidmoten.rtree.geometry.Geometries.line;
import static com.github.davidmoten.rtree.geometry.Geometries.point;
import static input.counts.selectors.utils.distToLineSegment;

/**
 * Created by Andrew A. Campbell on 1/30/18.
 */
public class TestDistToLineSegment {

	public static void main(String[] args) {
		System.out.println("Testing distToLineSegment:" );
		Line[] lines = new Line[8];
		int i = 0;
		// Build up an array of lines. Includes lines of zero length from (0,0) to (0,0)
		for (int x : Arrays.asList(0,2)){
			for (int y : Arrays.asList(0,2)){
				lines[2*i] = line(0, 0, x, y);
				lines[2*i + 1] = line(x, y, 0, 0);
				i++;
			}
		}
		// Array of points to test distances for
		Point[] points = new Point[6];
		points[0] = point(1,0);
		points[1] = point(1, 1);
		points[2] = point(0, 1);
		points[3] = point(-1, 1);
		points[4] = point(-1, 0);
		points[5] = point(-1, -1);

		System.out.println();
		SimpleClosest sC = new SimpleClosest();
		for (Line l : lines){
			System.out.println("Line: (" + l.x1() + ", " + l.y1() + ") --> " + "(" + l.x2() + ", " + l.y2() + ")");
			for (Point p : points){
				System.out.println("Dist to point: (" + p.x() + ", " + p.y() + "): " + distToLineSegment(p, l));
//				System.out.println(sC.distToLineSegment(p, l));
			}
			System.out.println();
		}
	}
}
