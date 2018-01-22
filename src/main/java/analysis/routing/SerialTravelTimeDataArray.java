package analysis.routing;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.matsim.core.trafficmonitoring.TravelTimeDataArray;

/**
 * Created by Andrew A. Campbell on 7/5/16.
 */
public class SerialTravelTimeDataArray implements java.io.Serializable {

//    private final double[] timeSum;
//    private final int[] timeCnt;
    private  double[] travelTimes;
    private  String linkId;

    /**
     * Empty constructor is required for this to be serializable.
     */
    SerialTravelTimeDataArray(){}

    /**
     * Initialize from an original TravelTimeDataArray
     * @param linkId
     * @param ttDataArray
     */
    public SerialTravelTimeDataArray(String linkId, TravelTimeDataArray ttDataArray){
        // NOTE: There should be no need to expose the private ttDataAray.link field since whoever calls this should already
        // know the link Id.
        this.linkId = linkId;
        this.travelTimes = new double[0];
        // Copy all of the timeslots to this.travelTimes
        int i = 0;
        while(true){
            try{
                // We can use any time for the second parameter. It does not effect the output (AAC 16/07/05)
                this.travelTimes[i] = ttDataArray.getTravelTime(i, 3600*9);
            }
            catch (OutOfRangeException e){
                System.out.println("Completed populating SerialTravelTimeDataArray");
                break;
            }
        }
    }

    public double[] getTravelTimes() throws NullPointerException{
        return this.travelTimes;
    }


}
