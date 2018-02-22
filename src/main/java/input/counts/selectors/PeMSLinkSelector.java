package input.counts.selectors;

import com.conveyal.osmlib.OSM;
import com.conveyal.osmlib.OSMEntity;
import com.conveyal.osmlib.Relation;
import com.conveyal.osmlib.Way;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Line;
import com.github.davidmoten.rtree.geometry.Point;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import rx.Observable;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static input.counts.selectors.utils.distToLineSegment;

/**
 * Works with sesnor data from the Caltrans Peformance Monitoring System (PeMS). In addition to proximity, it also
 * uses other criteria for link selection:
 * <p>
 * 1) OSM highway tag value (distinguish between ramps and mainlines)
 * 2) The OSM ref (highway number)
 * 3)
 * 4) Number of lanes
 * <p>
 * Created by Andrew A. Campbell on 2/1/18.
 */
public class PeMSLinkSelector implements LinkSelector {

	private double keepDist = 0.0001;
	private HashMap<Long, ArrayList<Long>> wayToRelations = new HashMap<>();  // stores the ids of all appropriate
	// relations a way is a member of
	private OSM osm;
	private HashMap<String, CSVRecord> pemsMetaData = new HashMap<>();

	private HashMap<String, String> osmDirToPemsDir = new HashMap<String, String>();

	{
		osmDirToPemsDir.put("north", "N");
		osmDirToPemsDir.put("south", "S");
		osmDirToPemsDir.put("east", "E");
		osmDirToPemsDir.put("west", "W");
		osmDirToPemsDir.put("n", "N");
		osmDirToPemsDir.put("s", "s");
		osmDirToPemsDir.put("e", "E");
		osmDirToPemsDir.put("w", "W");
		osmDirToPemsDir.put("northbound", "N");
		osmDirToPemsDir.put("southbound", "S");
		osmDirToPemsDir.put("eastbound", "E");
		osmDirToPemsDir.put("westbound", "W");
	}

	private ArrayList<String> dirWords = new ArrayList<>();
	{
		dirWords.add("north");
		dirWords.add("south");
		dirWords.add("east");
		dirWords.add("west");
		dirWords.add("northbound");
		dirWords.add("southbound");
		dirWords.add("eastbound");
		dirWords.add("westbound");
	}

	private HashMap<String, String> pemsDirsToNames = new HashMap<String, String>();

	{
		pemsDirsToNames.put("N", "north");
		pemsDirsToNames.put("S", "south");
		pemsDirsToNames.put("E", "east");
		pemsDirsToNames.put("W", "west");
	}

	private ArrayList<String> pemsRampTypes = new ArrayList<>();

	{
		pemsRampTypes.add("FF");
		pemsRampTypes.add("FR");
		pemsRampTypes.add("OR");
	}

	private HashMap<Object, ArrayList<String>> notMatched = new HashMap<>();
	private HashMap<Object, ArrayList<String>> tempNotMatched = new HashMap<>();

	// Fields used by the filter loop
	private ArrayList<LineContainer> tempNearest = new ArrayList<>();
	private LineContainer tempGoodEnough = null;


	/**
	 * @param osm          The populated OSM object
	 * @param pemsMetaPath Path to a PeMS metadata file.
	 */
	public PeMSLinkSelector(OSM osm, String pemsMetaPath) {
		this.osm = osm;

		////
		// Map ways to relations
		////
		System.out.println("Populating ways --> relations map");
		ArrayList<String> netTypes = new ArrayList<>();
		netTypes.add("US:I");
		netTypes.add("US:US");
		netTypes.add("US:CA");
		for (Long relID : this.osm.relations.keySet()) {
			Relation rel = this.osm.relations.get(relID);
			if (!rel.hasTag("route")) {
				continue;
			}
			String rt = rel.getTag("route");
			String ntwrk = rel.getTag("network");
			// Filter to only keep road relations that are a member of state or national networks
			if (rt.equals("road") & netTypes.contains(ntwrk)) {
				// Iterate through member ways and add them to the map
				for (Relation.Member m : rel.members) {
					if (m.type.equals(OSMEntity.Type.WAY)) {  // really they should all be ways
						// If the way is already in the map, add the relation to its list
						if (this.wayToRelations.keySet().contains(m.id)) {  //m.id is the way id
							this.wayToRelations.get(m.id).add(relID);
						} else {  // else add the way to the map
							ArrayList<Long> mbmrRels = new ArrayList<>();
							mbmrRels.add(relID);
							this.wayToRelations.put(m.id, mbmrRels);
						}
					}
				}
			}
		}
		////
		// Load the PeMS metadata
		////
		try {
			Reader rdr = new FileReader(pemsMetaPath);
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(rdr);
			for (CSVRecord r : records) {
				String id = r.get("ID");
				this.pemsMetaData.put(id, r);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object selectLink(Observable<Entry<Long, Line>> links, Object pointID, Point point) {
		// Reset the temp containers
		this.tempNotMatched.clear();
		this.tempNearest.clear();
		this.tempGoodEnough = null;
		// populate LineContainer list
		for (Entry<Long, Line> e : links.toBlocking().toIterable()) {
			Line l = e.geometry();
			Double d = distToLineSegment(point, l);
			this.tempNearest.add(new LineContainer(l, d, e.value()));
		}
		Collections.sort(this.tempNearest); // now sorted by increasing distance from point

		// Iterate through all k-nearest until a match found or end reached
		for (int i = 0; i < this.tempNearest.size(); i++) {
			LineContainer lC = this.tempNearest.get(i);
			///
			// Filter 1 - Type
			////
			if (!this.checkType(lC, pointID)) {
				continue;
			}

			////
			// Filter 2 - Number
			////
			if (!this.checkNumber(lC, pointID)) {
				continue;
			}

			////
			// Filter 3 - Direction
			////
			if (this.checkDirection(lC, pointID)) {
				return lC.wayId;
			} else {
				continue;
			}
		}
		////
		// Return the good-enough or null
		////
		Long out = (!(this.tempGoodEnough == null)) ? this.tempGoodEnough.wayId : null;
		if (out == null){
			this.notMatched.putAll(this.tempNotMatched);
		} else {
			System.out.println("GOOD ENOUGH");
		}
		return out;
	}

	@Override
	public HashMap<Object, ArrayList<String>> getNotMachedLinks() {
		return this.notMatched;
	}

	/**
	 * Checks to make sure we are not matching a mainline with a ramp or visa versa.
	 *
	 * @param lC
	 * @param pointID
	 * @return false if the types don't match. true otherwise
	 */
	private Boolean checkType(LineContainer lC, Object pointID) {
		String osmHwyTag = this.osm.ways.get(lC.wayId).getTag("highway");
		String pemsType = this.pemsMetaData.get(pointID).get("Type");
		// Check if (PeMS sensor is on ramp AND way is not) OR (PeMS is ML AND way is a ramp)
		if ((this.pemsRampTypes.contains(pemsType) & !osmHwyTag.contains("_link")) |
				(pemsType.equals("ML") & osmHwyTag.contains("_link"))) {
			this.addToTempNotMatched(pointID, "TYPE");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks if we can match the freeway number. Returns false if not.
	 *
	 * @param lC
	 * @param pointID
	 * @return
	 */
	private Boolean checkNumber(LineContainer lC, Object pointID) {
		Way way = this.osm.ways.get(lC.wayId);
		String pemsNumber = this.pemsMetaData.get(pointID).get("Fwy");
		// Check for a matching ref tag
		if (way.hasTag("ref")) {
			ArrayList<String> refParts = new ArrayList(Arrays.asList(way.getTag("ref").split("[^\\w]+")));
			if (refParts.contains(pemsNumber)) {
				return true;  // able to match numbers based on the ref parts
			}
		} else if (way.hasTag("name")) { // no ref tag, check the name instead
			ArrayList<String> nameParts = new ArrayList<>(Arrays.asList(way.
					getTag("name").split("[^\\w]+")));
			if (nameParts.contains(pemsNumber)) {
				return true;
			}
		}
		// Could not match ref nor name
		this.addToTempNotMatched(pointID, "NUMBER");
		return false;
	}

	private Boolean checkDirection(LineContainer lC, Object pointID) {
		Way way = this.osm.ways.get(lC.wayId);
		String pemsDir = this.pemsMetaData.get(pointID).get("Dir");
		String pemsDirName = this.pemsDirsToNames.get(pemsDir);

		if (this.wayToRelations.containsKey(lC.wayId)) {
			// Way is in a Relation, loop over all relations to try and match/reject direction
			for (Long relID : this.wayToRelations.get(lC.wayId)) {
				Relation rel = this.osm.relations.get(relID);
				if (rel.hasTag("name")) {
					// check if direction is in name
					if (rel.getTag("name").toLowerCase().contains(pemsDirName)) {
						System.out.println("MATCHED DIRECTION IN NAME");
						return true;
					} else { // check for direction miss-match
						HashSet<String> dirWordsSet = new HashSet<>(this.dirWords);
						HashSet<String> nameSet = new HashSet<>(Arrays.asList(rel.getTag("name").toLowerCase()
								.split("[^\\w]+")));  // set of all words in the direction tag value
						dirWordsSet.retainAll(nameSet);  // take intersection with dirWordsSet
						if (!dirWordsSet.isEmpty()) {
							// we found a direction in the name, but it can't be the one we're looking for because we
							// checked that above
							addToTempNotMatched(pointID, "WRONG DIRECTION (NAME)");
							return false;
						}
					}
				}
				if (rel.hasTag("direction")) {
					// check if the direction value is the key set, i.e. we recognize and can map it
					if (this.osmDirToPemsDir.containsKey(rel.getTag("direction").toLowerCase())) {
						String relPemsDir = this.osmDirToPemsDir.get(rel.getTag("direction").toLowerCase());
						// relation direction mapped to the PeMS directions
						if (relPemsDir.equals(pemsDir)) {
							System.out.println("MATCHED DIRECTION IN DIRECTION");
							return true;
						} else { // must have direction miss-match
							this.addToTempNotMatched(pointID, "WRONG DIRECTION (DIRECTION)");
							return false;
						}
					} else {  // can't map the direction tag to the pems direction words
						this.addToTempNotMatched(pointID, "DIRECTION TAG: " + rel.getTag("direction"));
						return false;
					}
				}
			}
		}
		// Could not accept nor reject based on direction, potentially add this as a candidate
		if (this.tempGoodEnough == null) {
			if (lC.dist <= this.keepDist) {  // only update if it is close enough
				this.tempGoodEnough = lC;
			}
		} else {
			this.tempGoodEnough = ((lC.dist < this.tempGoodEnough.dist) && (lC.dist < this.keepDist)) ? lC : this.tempGoodEnough;
		}
		return false;
	}


	private void addToTempNotMatched(Object pointID, String reason) {
		if (this.tempNotMatched.keySet().contains(pointID)) {
			this.tempNotMatched.get(pointID).add(reason);
		} else {
			ArrayList<String> rA = new ArrayList<>(Arrays.asList(reason));
			this.tempNotMatched.put(pointID, rA);
		}
	}

	private class LineContainer implements Comparable<LineContainer> {
		Line line;
		Double dist;
		Long wayId;

		LineContainer(Line l, Double d, Long i) {
			this.line = l;
			this.dist = d;
			this.wayId = i;
		}

		@Override
		public int compareTo(LineContainer o) {
			return this.dist.compareTo(o.dist);
		}
	}
}


