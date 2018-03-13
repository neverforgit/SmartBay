package input.counts;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import java.util.HashMap;

/**
 * Created by Andrew A. Campbell on 3/6/18.
 *
 * Used to map the traffic sensor IDs to MATSim Network Link ids.
 */
public class SensorToLinkMatcher {

	private HashMap<Object, Long> sensors2Ways;
	private HashMap<Long, Id<Link>> ways2Links;
	private Network matsimNet;
	Logger log = Logger.getLogger(SensorToLinkMatcher.class);

	private HashMap<Object, Id<Link>> sensorsToLinks = new HashMap<>();

	/**
	 *
	 * @param sensors2Ways
	 * @param ways2Links
	 * @param cleanedNet A MATSim network that has, presumably, been passed through NetworkCleaner
	 */
	public SensorToLinkMatcher(HashMap<Object, Long> sensors2Ways, HashMap<Long, Id<Link>> ways2Links, Network cleanedNet) {
		this.sensors2Ways = sensors2Ways;
		this.ways2Links = ways2Links;
		this.matsimNet = cleanedNet;
	}

	public void matchSensorsToLinks(){
		for (Object sensorID : this.sensors2Ways.keySet()){
			Long wayID = this.sensors2Ways.get(sensorID);
			Id<Link> linkId = this.ways2Links.get(wayID);
			// check if the link ID is still in the network. NetworkCleaner may have removed it
			if (this.matsimNet.getLinks().keySet().contains(linkId)) {
				this.sensorsToLinks.put(sensorID, linkId);
			} else {
				log.warn("LINK ID: " + linkId.toString() + " not in network");
			}
		}
	}

	public HashMap<Object, Id<Link>> getSensorsToLinks(){
		return this.sensorsToLinks;
	}

	public void writeMAtches(String outPath){

	}
}
