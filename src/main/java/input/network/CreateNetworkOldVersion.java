package input.network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

//import org.matsim.core.api.experimental.network.NetworkWriter;

/**
 * Transforms an OSM network into the appropriate MATSim network format.
 * <p>
 * USAGE: You need to define the coordinate projection system and some file paths.
 * <p>
 * COORDINATE PROJECTION SYSTEM:
 * -Lookup the EPSG code for the projection system you are using. We normally use UTM Standard, you
 * just need to find the correct zone. A good source for EPSG codes is:
 * http://spatialreference.org/ref/epsg/
 * <p>
 * PATHS TO DEFINE:
 * 0 - input OSM file
 * 1 - output network file*
 * 2 - epsg code (e.g. EPSG:4326)
 */
public class CreateNetworkOldVersion {

	public static void main(String[] args) {
		////
		// PATHS TO DEFINE
		////

		//String osm = "./input/NewDelhi.osm"; //input OSM file
		//String outNet = "./input/network_New_Delhi.xml"; //output network file

		String osm = args[0];
		String outNet = args[1];
		String epsgCode = args[2];

		////
		// COORDINATE PROJECTION SYSTEM
		////
		//String epsgCode = "EPSG:32643";


		Config config = ConfigUtils.createConfig();
		Scenario sc = ScenarioUtils.createScenario(config);
		Network net = sc.getNetwork();
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, epsgCode);
		OsmNetworkReader onr = new OsmNetworkReader(net, ct);
		//Tell MATSim to preserve the original network geometry, creating a much greater number of nodes and links
		//Comment out to setKeePaths to let MATSim simplify (this should be the default option)
//		onr.setKeepPaths(true);
		onr.parse(osm);
		new NetworkCleaner().run(net);
		new NetworkWriter(net).write(outNet);
	}

}