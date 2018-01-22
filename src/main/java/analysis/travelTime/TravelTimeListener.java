package analysis.travelTime;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.trafficmonitoring.DataContainerProvider;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.trafficmonitoring.TravelTimeDataArray;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Andrew A. Campbell on 9/20/16.
 *
 * This listener is used to validate travel times on all links with free flow speed above a given cutoff. It creates a
 * file that can be used for calculating total and average delays on qualifying links. For each qualifying link, it
 * writes the link_Id and free flow time. It also writes the total travel time observed in each time bin and total
 * count observed in each time bin.
 *
 * This is nicer than parsing the events file because it takes advantage of a TravelTimeCalculator object that already
 * exists at the end of each mobsim.
 */

public class TravelTimeListener implements IterationEndsListener {
	private double congestionSpeed;
	private double fwySpeed;  // defines the minimum freespeed for links to be validated.
	private List<Integer> iterations;  // iterations to calculate travel time validations
	private HashMap<Id<Link>,double[]> linkTravelTimes = new HashMap<Id<Link>, double[]>();
	private HashMap<Id<Link>, int[]> linkCounts = new HashMap<Id<Link>, int[]>();
	private HashMap<Id<Link>, double[]> linkSums = new HashMap<Id<Link>, double[]>();
	private String outPutDir;
	private Scenario scenario;
	private double simHours;
	private TravelTimeCalculator ttCalc;
	private boolean writeSummary = false;


	/**
	 *
	 * @param fwySpeed Minimum speed for links to be validated in meters-per-second. Set to 33.3 to only consider
	 *                    freeway links.
	 * @param iterations List of iterations to run the validation on.
	 * @param outDir Path to output directory.
	 * @param simHours Number of hours in the simulation.
	 */
	public TravelTimeListener(double fwySpeed, List<Integer> iterations, String outDir, double simHours){
		this.fwySpeed = fwySpeed;
		this.iterations = iterations;
		this.outPutDir = outDir;
		this.simHours = simHours;
	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		if (this.iterations.contains(event.getIteration())) {
			// Get the travel time
			// TODO - check for null values in the ttCalc. It seems that is possible for the provider to yield an
			// empty ttCalc field
			this.scenario = event.getServices().getScenario();
			this.ttCalc = event.getServices().getInjector().getProvider(TravelTimeCalculator.class).get();
			////
			// Expose the TravelTimeDataArray for every link to get times and counts
			////
			Field providerField, travelTimeDataArrayField, travelTimesField, travelCountsField, travelTimeSumField;
			try {
				providerField = this.ttCalc.getClass().getDeclaredField("dataContainerProvider");
				providerField.setAccessible(true);
				DataContainerProvider provider = (DataContainerProvider) providerField.get(this.ttCalc);

				// Iterate through all links and
				for (Id<Link> id : this.scenario.getNetwork().getLinks().keySet()) {
					if (this.scenario.getNetwork().getLinks().get(id).getFreespeed() >= this.fwySpeed) {
						// expose the TravelTimeDataArray
						Object data = provider.getTravelTimeData(id, true);
						travelTimeDataArrayField = data.getClass().getDeclaredField("ttData");
						travelTimeDataArrayField.setAccessible(true);
						TravelTimeDataArray ttDataArray = (TravelTimeDataArray) travelTimeDataArrayField.get(data);
						// get the travelTimes
						travelTimesField = ttDataArray.getClass().getDeclaredField("travelTimes");
						travelTimesField.setAccessible(true);
						double[] travelTimes = (double[]) travelTimesField.get(ttDataArray);
						this.linkTravelTimes.put(id, travelTimes);
						// get the counts
						travelCountsField = ttDataArray.getClass().getDeclaredField("timeCnt");
						travelCountsField.setAccessible(true);
						int[] counts = (int[]) travelCountsField.get(ttDataArray);
						this.linkCounts.put(id, counts);
						// get the time sums
						travelTimeSumField = ttDataArray.getClass().getDeclaredField("timeSum");
						travelTimeSumField.setAccessible(true);
						double[] timeSums = (double[]) travelTimeSumField.get(ttDataArray);
						this.linkSums.put(id, timeSums);
					}
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			////
			// Write the output
			////
			try {
				writeOutput(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (this.writeSummary){
				try {
					this.writeSummaryOutput(event);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			// clear the fields
			this.linkCounts = new HashMap<Id<Link>, int[]>();
			this.linkTravelTimes = new HashMap<Id<Link>, double[]>();
			this.linkSums = new HashMap<Id<Link>, double[]>();
			this.scenario = null;
			this.ttCalc = null;
		}
	}

	private void writeOutput(IterationEndsEvent event) throws IOException{
		String runId = event.getServices().getScenario().getConfig().controler().getRunId();
		String outFilePath = Paths.get(this.outPutDir,
				MessageFormat.format("ITERS/it.{0}/{1}.{0}.ValidationTravelTimeListener.txt", event.getIteration(), runId)).
				toString();
		// build the header
		StringJoiner header = new StringJoiner("\t");
		header.add("link_id");
		header.add("link_dist");
		header.add("ff_time");
		int nBins = this.linkCounts.get(this.scenario.getNetwork().getLinks().keySet().iterator().next()).length;
		// add time bins to header
		StringJoiner ttBins = new StringJoiner("\t");
		StringJoiner countBins = new StringJoiner("\t");
		int incr = this.ttCalc.getTimeSlice();
		for (int n=1; n<=nBins; n++){
			ttBins.add("tt_" + (n-1)*incr + "_" + n*incr);
			countBins.add("cnt_" + (n-1)*incr + "_" + n*incr );
		}
		header.merge(ttBins);
		header.add(countBins.toString() + "\n");
		// write
		BufferedWriter writer = IOUtils.getBufferedWriter(outFilePath);
		writer.write(header.toString());
		Network network = this.scenario.getNetwork();
		for (Id<Link> id : this.linkCounts.keySet()) {
			Link link = network.getLinks().get(id);
			// id, dist and tt_time
			StringJoiner row = new StringJoiner("\t");
			row.add(id.toString());
			row.add(String.valueOf(link.getLength()));
			row.add(String.valueOf(link.getLength() / link.getFreespeed())); // free flow travel time
			// travel times and counts
			StringJoiner times = new StringJoiner("\t");
			StringJoiner counts = new StringJoiner("\t");
			for (int i = 0; i < nBins; i++) {
				times.add(String.valueOf(this.linkSums.get(id)[i]));
				counts.add(String.valueOf(this.linkCounts.get(id)[i]));
			}
			String values = String.join("\t", times.toString(), counts.toString());
			// write the row
			row.add(values);
			writer.write(row.toString() + "\n");
		}
		writer.close();
	}

	/**
	 * Sets the flag to write the summary file. Also defines congestion speed.
	 * @param congestionSpeed Speed below which traffic is defined as congested. In meters per second.
	 */
	public void setWriteSummary(double congestionSpeed){
		this.writeSummary = true;
		this.congestionSpeed = congestionSpeed;
	}

	private void writeSummaryOutput(IterationEndsEvent event) throws IOException{
		// build output file path
		String runId = event.getServices().getConfig().controler().getRunId();
		String outFilePath = Paths.get(this.outPutDir,
				MessageFormat.format("ITERS/it.{0}/{1}.{0}.ValidationTravelTimeListenerSummary.txt", event.getIteration(), runId)).
				toString();
		// build the header
		StringJoiner header = new StringJoiner("\t");
		header.add("link_id");
		header.add("link_dist");
		header.add("avg_delay");
		header.add("avg_congested_delay");
		header.add("total_count");
		header.add("total_delayed_count");
		header.add("total_congested_count");
		// write
		BufferedWriter writer = IOUtils.getBufferedWriter(outFilePath);
		writer.write(header.toString() + "\n");
		Network network = this.scenario.getNetwork();
		int nBins = this.linkCounts.get(this.scenario.getNetwork().getLinks().keySet().iterator().next()).length;
		for (Id<Link> id : this.linkCounts.keySet()){
			Link link = network.getLinks().get(id);
			// id, dist and tt_time
			StringJoiner row = new StringJoiner("\t");
			row.add(id.toString());
			row.add(String.valueOf(link.getLength()));
			// get delayed and congested times and counts
			int total_count = 0;
			double total_delay = 0;
			int total_delayed_count = 0;
			double total_congested_delay = 0;
			int total_congtested_count = 0;
			for (int i=0; i < nBins; i++){
				double binSum = this.linkSums.get(id)[i]; //total travel time in this bin
				int binCount = this.linkCounts.get(id)[i];  //total count in this bin
				total_count += binCount;
				//check for delay: avg_tt > freeflow_tt
				if (binSum/binCount > (link.getLength()/link.getFreespeed())){
					total_delay += binSum;
					total_delayed_count += binCount;
				}
				// check for congestion
				if (binSum/binCount > (link.getLength()/this.congestionSpeed)){
					total_congested_delay += binSum;
					total_congtested_count += binCount;
				}
			}
			if (total_count != 0) {
				row.add(String.valueOf(total_delay / total_count));
				row.add(String.valueOf(total_congested_delay / total_count));
				row.add(String.valueOf(total_count));
				row.add(String.valueOf(total_delayed_count));
				row.add(String.valueOf(total_congtested_count));
				writer.write(row.toString() + "\n");
			}
			else {
				row.add("0\t0\t0\t0\t0\n");
				writer.write(row.toString());
			}
		}
		writer.close();
	}
}
