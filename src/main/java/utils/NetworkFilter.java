package utils;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 * This class is used to filter out links in a network by different criteria.
 *
 * Created by Andrew A. Campbell on 3/23/17.
 */
public class NetworkFilter {

	/**
	 * Returns a new network whose links only support the modes in the input array.
	 * @param keepModes Names of modes we want to keep.
	 * @return
	 */
	public static Network filterByModesToKeep(Network origNetwork, ArrayList<String> keepModes){
		Network outNet = NetworkUtils.createNetwork();
		for (Link link : origNetwork.getLinks().values()){
			boolean keepLink = false;
			// check if this link allows one of the modes to keep
			for (String mode : link.getAllowedModes()){
				if (keepModes.contains(mode)){
					keepLink = true;
					break;
				}
			}
			// add the link if we want to keep it
			if (keepLink){
				cloneLink(outNet, link);
			}
		}
		return outNet;
	}


	/**
	 * UNTESTED (AAC 17/03/23)
	 * Removes a link if contains any of the modes to remove.
	 * @param removeModes
	 * @return
	 */
	public static Network filterByModesToRemoveAny(Network origNetwork, ArrayList<String> removeModes){
		Network outNet = NetworkUtils.createNetwork();
		for (Link link : origNetwork.getLinks().values()){
			boolean keepLink = true;
			// check if this link allows one of the modes to remove
			for (String mode : link.getAllowedModes()){
				if (removeModes.contains(mode)){
					keepLink = false;
					break;
				}
			}
			// add the link if we want to keep it
			if (keepLink){
				cloneLink(outNet, link);
			}
		}
		return outNet;
	}

	/**
	 * UNTESTED (AAC 17/03/23)
	 * Removes a link if all its allowed modes are in the remove list.
	 * @param removeModes
	 * @return
	 */
	public static Network filterByModesToRemoveAll(Network origNetwork, ArrayList<String> removeModes){
		Network outNet = NetworkUtils.createNetwork();
		for (Link link : origNetwork.getLinks().values()){
			boolean keepLink = true;
			Set<String> linkModes = link.getAllowedModes();
			boolean[] modeChecks = new boolean[linkModes.size()];
			Arrays.fill(modeChecks, false);
			// check if each of the link's modes are on the remove list
			int i = 0;
			for (String mode : link.getAllowedModes()){
				if (removeModes.contains(mode)){
					modeChecks[i] = true;
				}
				i++;
			}
			// if all modes are on remove list, don't keep the link
			if (!Arrays.asList(modeChecks).contains(false)){
				keepLink = false;
			}

			// add the link if we want to keep it
			if (keepLink){
				cloneLink(outNet, link);
			}
		}
		return outNet;
	}

	/**
	 * Clones a link, and its to/from nodes, from the original network to the new one.
	 * @param newNetwork
	 * @param origLink
	 */
	private static void cloneLink(Network newNetwork, Link origLink){
		if (!newNetwork.getNodes().containsKey(origLink.getFromNode().getId())){
			Node oFN = origLink.getFromNode();
			Node fromNode = NetworkUtils.createNode(oFN.getId(), oFN.getCoord());
			newNetwork.addNode(fromNode);
		}
		if (!newNetwork.getNodes().containsKey(origLink.getToNode().getId())){
			Node oTN = origLink.getToNode();
			Node toNode = NetworkUtils.createNode(oTN.getId(), oTN.getCoord());
			newNetwork.addNode(toNode);
		}
		if (!newNetwork.getLinks().containsKey(origLink.getId())) {
			Link newLink = NetworkUtils.createLink(origLink.getId(), newNetwork.getNodes().get(origLink.getFromNode().getId()),
					newNetwork.getNodes().get(origLink.getToNode().getId()), newNetwork, origLink.getLength(),
					origLink.getFreespeed(), origLink.getCapacity(), origLink.getNumberOfLanes());
			newNetwork.addLink(newLink);
		}
	}


	/**
	 * Test filterByModesToKeep
	 * Sys args:
	 * 0 - path to original network
	 * 1 - comma-separated list of modes to keep
	 * 2 - path to write output network
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		String netPath = args[0];
		String[] modesToKeep = args[1].split(",");
		ArrayList<String> modesToKeepList = new ArrayList<>(Arrays.asList(modesToKeep));

		//Initialize original multimodal network
		Network origNetwork = NetworkUtils.createNetwork();
		new MatsimNetworkReader(origNetwork).readFile(netPath);

		//Create filetered network
		Network newNet = filterByModesToKeep(origNetwork, modesToKeepList);
		new NetworkWriter(newNet).write(args[2]);

	}

}
