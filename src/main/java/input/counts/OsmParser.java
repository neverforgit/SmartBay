package input.counts;

import com.conveyal.osmlib.Node;
import com.conveyal.osmlib.OSM;
import com.conveyal.osmlib.Way;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Line;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static com.github.davidmoten.rtree.geometry.Geometries.line;
import static input.counts.OsmParser.FilterType.INCLUDE;


/**
 * Reads the raw OSM pbf file to make a mapdb and RTree of the network.
 *
 * Created by Andrew A. Campbell on 1/26/18.
 */
public class OsmParser {
	private String osmFile; //path to OSM pbf (or vex?) file
	private OSM osm;
	private FilterType filterType = INCLUDE;
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


	OsmParser(String osmFile) {
		this.osmFile = osmFile;
		File dir = new File(osmFile).getParentFile();
		this.osm = new OSM(new File(dir,"osm.mapdb").getPath());
		osm.intersectionDetection = true;
		osm.readFromFile(osmFile);
	}

	//TODO - should break out building the tree into a separate class
	public void makeRTree(){
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
	}

	/**
	 * Adds each of the individual lengths (node-to-node straight line) of the Way to the RTree.
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
			System.out.println("Bad Way, ID: " + wayID);
			return;
		}
		double fromY = fromNd.getLat();
		for (int i=1 ; i<nodes.length; i++){
			Node toNode = osm.nodes.get(nodes[i]);
			double toX = -1.0;
			try {
				toX = toNode.getLon();
			} catch (NullPointerException e) {
				System.out.println("Bad Way, ID: " + wayID);
				return;
			}
			double toY = toNode.getLat();
			this.tree = this.tree.add(wayID, line(fromX, fromY, toX, toY));
			fromX = toX;
			fromY = toY;
		}
	}


	/**
	 * Write a serializable RTree to the output dir.
	 */
	public void serializeRTree(){

	}

	/**
	 * Sets the link filtering method. Valid options are: INCLUDE, EXCLUDE, and ALL
	 * @param type
	 */
	public void setFilterType(FilterType type){
		this.filterType = type;
	}


}
