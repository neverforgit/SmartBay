package input.counts;

import com.conveyal.osmlib.Node;
import com.conveyal.osmlib.OSM;
import com.conveyal.osmlib.Way;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializer;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Line;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.github.davidmoten.rtree.geometry.Geometries.line;
import static input.counts.OsmRTree.FilterType.INCLUDE;

/**
 * Created by Andrew A. Campbell on 1/31/18.
 */
public class OsmRTree {

	private OsmRTree.FilterType filterType = INCLUDE;
	private OSM osm;
	private HashMap<Long, String> badWays = new HashMap();

	public RTree<Long, Line> tree = RTree.create();

	protected enum FilterType {
		INCLUDE,  // provide a list of highway types to include
		EXCLUDE,  // provide a list of highway types to exclude
		ALL  /// include everything
	}

	public ArrayList<String> includeHighways = new ArrayList<String>() {{
		// default values are for highways ("restricted access major divided highway")
		add("motorway");
		add("motorway_link");
	}};

	public ArrayList<String> excludeHighways = new ArrayList<String>();


	/**
	 * A fully loaded (not empty) OSM read from file or mapdb.
	 * @param osm
	 */
	public OsmRTree(OSM osm){
		this.osm = osm;
	}

	/**
	 * Populates and RTree with line geometries for every segment of road in the OSM. A single OSM "way" consists
	 * of one or more segments. It is possible to use the setFilterType, and the includeHighways / excludeHighways
	 * fields to filter that construction of the RTree by the value of the OSM highway tag of the ways (e.g. only include "motorways",
	 * or exclude all "pedestrian" ways).
	 */
	public void makeRTree(){
		System.out.println("Building RTree");
		Map<Long, Way> ways = this.osm.ways;
		for (Long wayID : ways.keySet()){
			Way way = ways.get(wayID);
			String hwy = way.getTag("highway");
			switch (this.filterType)
			{
				case INCLUDE:
					if (this.includeHighways.contains(hwy)){
						addWay(wayID, way);
					}
					break;
				case EXCLUDE:
					if (!this.excludeHighways.contains(hwy)){
						addWay(wayID, way);
					}
					break;
				case ALL:
					addWay(wayID, way);
					break;
				default:
					System.out.println("Invalid filter type.");
					break;
			}
		}
		System.out.println("Done building RTree. " + this.badWays.size() + " ways could not be added.");
	}

	/**
	 * Adds each of the individual segments (node-to-node straight line) of the Way to the RTree.
	 * @param wayID
	 * @param way
	 */
	public void addWay(Long wayID, Way way){
		long[] nodes = way.nodes;
		Node fromNd = osm.nodes.get(nodes[0]);
		double fromX = -1.0;
		try {  // in case of semantic error in raw OSM file (i.e. way includes nodes that don't exist)
			fromX = fromNd.getLon();
		} catch	(java.lang.NullPointerException e) {
			this.badWays.put(wayID, "Bad node");
			return;
		}
		double fromY = fromNd.getLat();
		for (int i=1 ; i<nodes.length; i++){
			Node toNode = osm.nodes.get(nodes[i]);
			double toX = -1.0;
			try {
				toX = toNode.getLon();
			} catch (NullPointerException e) {
				this.badWays.put(wayID, "Bad node");
				return;
			}
			double toY = toNode.getLat();
			this.tree = this.tree.add(wayID, line(fromX, fromY, toX, toY));
			fromX = toX;
			fromY = toY;
		}
	}

	/**
	 * Write a serializable RTree to the output path.
	 *@param outPath Path to write serialized RTree.
	 */
	public void serializeRTree(String outPath) throws IOException {
		FileOutputStream os = new FileOutputStream(outPath);
		Serializer<Long, Line> serializer = Serializers.flatBuffers().javaIo();
		serializer.write(this.tree, os);
	}

	/**
	 * Sets the link filtering method. Valid options are: INCLUDE, EXCLUDE, and ALL.
	 * @param type
	 */
	public void setFilterType(FilterType type){
		this.filterType = type;
	}

	public HashMap<Long, String> getBadWays() {
		return badWays;
	}
}
