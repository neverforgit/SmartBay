package analysis.routing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.trafficmonitoring.TravelTimeDataArray;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Andrew A. Campbell on 7/5/16.
 */
public class SerialLinkData implements java.io.Serializable {

    private Map<String, SerialTravelTimeDataArray> serialData;

    /**
     * Empty constructor is required for this to be serializable.
     */
    SerialLinkData(){}

    /**
     * This constructor builds the serializable from a populated TravelTimeCalculator. We will call this from
     * the controler listener to create the serializable object at the end of a mobsim iteration.
     * @param ttCalc
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    public SerialLinkData(TravelTimeCalculator ttCalc) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Field linkDataField = ttCalc.getClass().getDeclaredField("linkData");
        linkDataField.setAccessible(true);
        // Get the private nested DataContainer class
        //TODO next line breaks with a sun.reflect.UnsafeFieldAccessorImpl.throwSetIllegalArgumentException
        Map<Id<Link>, Object> linkData =  (Map<Id<Link>, Object>) linkDataField.get(this);

        // Loop through and populate serialData
        for (Id<Link> id: linkData.keySet()){
            this.serialData.put(id.toString(), new SerialTravelTimeDataArray(id.toString(),
                    (TravelTimeDataArray) linkData.get(id)));
        }
    }


    public Map<String, SerialTravelTimeDataArray> getSerialData() throws NullPointerException{
        return this.serialData;
    }

    /**
     *
     * @param outPath Path to write the serialized object.
     * @throws IOException
     */
    public void serializeData(String outPath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(outPath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this.serialData);
        out.close();
        fileOut.close();
    }
}
