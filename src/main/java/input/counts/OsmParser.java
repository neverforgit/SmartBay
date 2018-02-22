package input.counts;

import com.conveyal.osmlib.OSM;

import java.io.File;


/**
 * Reads the raw OSM pbf file to make a mapdb and RTree of the network.
 *
 * Created by Andrew A. Campbell on 1/26/18.
 */
public class OsmParser {
	private String osmFile; //path to OSM pbf (or vex?) file
	private OSM osm;

	public OsmParser(String osmFile) {
		this.osmFile = osmFile;
		File dir = new File(osmFile).getParentFile();
		this.osm = new OSM(new File(dir,"osm.mapdb").getPath());
		this.osm.intersectionDetection = true;
		this.osm.readFromFile(osmFile);
	}

	public OSM getOsm(){
		return this.osm;
	}
}
