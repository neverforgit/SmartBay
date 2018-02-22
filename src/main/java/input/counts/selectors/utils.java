package input.counts.selectors;

import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;

/**
 * Created by Andrew A. Campbell on 2/1/18.
 */
public class utils {

	public static double distToLineSegment(Point p, Line l) {

		float[] a = new float[2];
		a[0] = p.x() - l.x1();
		a[1] = p.y() - l.y1();
		float[] aPrime = new float[2];
		aPrime[0] = p.x() - l.x2();
		aPrime[1] = p.y() - l.y2();

		float[] b = new float[2];
		b[0] = l.x2() - l.x1();
		b[1] = l.y2() - l.y1();
		float[] bPrime = new float[2];
		bPrime[0] = l.x1() - l.x2();
		bPrime[1] = l.y1() - l.y2();

		float dot = a[0]*b[0] + a[1]*b[1];
//		System.out.println("dot " + dot);
		float dotPrime = aPrime[0]*bPrime[0] + aPrime[1]*bPrime[1];
//		System.out.println("dotPrime "+ dotPrime);
		// Check if point lies outside perpendicular plane
		if (dot < 0 || dotPrime < 0){  // If point outside plane, return dist to closest end of line segment
			double d1 = Math.sqrt(Math.pow(p.x() - l.x1(), 2) + Math.pow(p.y() - l.y1(), 2));
			double d2 = Math.sqrt(Math.pow(p.x() - l.x2(), 2) + Math.pow(p.y() - l.y2(), 2));
			return (d1 < d2) ? d1 : d2;
		} else {  // return orthogonal distance to line
			double d = b[1]*p.x();
			double e = b[0]*p.y();
			double f =  l.x2()*l.y1();
			double g =  l.x1()*l.y2();
			double h = d - e + f - g;
			double i = Math.abs(h);
			double j = Math.abs(d - e + f - g);
//			double num = Math.abs(b[1]*p.x() - b[0]*p.y() + l.x2()*l.y1() - l.x1()*l.y2());
			double num = Math.abs(h);
			double denom = Math.sqrt(Math.pow(l.y2() - l.y1(), 2) + Math.pow(l.x2() - l.x1(), 2));
			double out = num / denom;
			return num / denom;
		}
	}

//	public static double distToLineSegmentBigDouble(Point p, Line l) {
//
//		float[] a = new float[2];
//		a[0] = p.x() - l.x1();
//		a[1] = p.y() - l.y1();
//		float[] aPrime = new float[2];
//		aPrime[0] = p.x() - l.x2();
//		aPrime[1] = p.y() - l.y2();
//
//		float[] b = new float[2];
//		b[0] = l.x2() - l.x1();
//		b[1] = l.y2() - l.y1();
//		float[] bPrime = new float[2];
//		bPrime[0] = l.x1() - l.x2();
//		bPrime[1] = l.y1() - l.y2();
//
//		float dot = a[0]*b[0] + a[1]*b[1];
////		System.out.println("dot " + dot);
//		float dotPrime = aPrime[0]*bPrime[0] + aPrime[1]*bPrime[1];
////		System.out.println("dotPrime "+ dotPrime);
//		// Check if point lies outside perpendicular plane
//		if (dot < 0 || dotPrime < 0){  // If point outside plane, return dist to closest end of line segment
//			double d1 = Math.sqrt(Math.pow(p.x() - l.x1(), 2) + Math.pow(p.y() - l.y1(), 2));
//			double d2 = Math.sqrt(Math.pow(p.x() - l.x2(), 2) + Math.pow(p.y() - l.y2(), 2));
//			return (d1 < d2) ? d1 : d2;
//		} else {  // return orthogonal distance to line
//			double d = b[1]*p.x();
//			double e = b[0]*p.y();
//			double f =  l.x2()*l.y1();
//			double g =  l.x1()*l.y2();
//			double h = d - e + f - g;
//			double i = Math.abs(h);
//			double j = Math.abs(d - e + f - g);
////			double num = Math.abs(b[1]*p.x() - b[0]*p.y() + l.x2()*l.y1() - l.x1()*l.y2());
//			double num = Math.abs(i);
//			double denom = Math.sqrt(Math.pow(l.y2() - l.y1(), 2) + Math.pow(l.x2() - l.x1(), 2));
//			double out = num / denom;
//			return num / denom;
//		}
//	}

	/**
	 * Returns the index of the smallest value.
	 * @param a
	 * @return
	 */
	public static int argMin(double[] a){
		double min = Double.MAX_VALUE;
		int i = 0;
		int idx = -1;
		for (double d : a){
			if (d < min){
				idx = i;
				min = d;
			}
			i++;
		}
		return idx;
	}
}
