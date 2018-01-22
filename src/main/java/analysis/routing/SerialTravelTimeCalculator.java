package analysis.routing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.groups.TravelTimeCalculatorConfigGroup;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.trafficmonitoring.TravelTimeData;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Andrew A. Campbell on 7/1/16.
 *
 * This class allows you to serialize the linkData field. You can also fill the linkData field from
 * a serialized object. This allows us to skip event listening to populate the link travel times.
 *
 */
public class SerialTravelTimeCalculator extends TravelTimeCalculator implements Serializable{
    public SerialTravelTimeCalculator(Network network, int timeslice, int maxTime, TravelTimeCalculatorConfigGroup ttconfigGroup) {
        super(network, timeslice, maxTime, ttconfigGroup);
    }

    public SerialTravelTimeCalculator(Network network, TravelTimeCalculatorConfigGroup ttconfigGroup) {
        super(network, ttconfigGroup);
    }

     static class DataContainer implements Serializable {
         final TravelTimeData ttData;
         volatile boolean needsConsolidation = false;
         DataContainer(final TravelTimeData data) {
            this.ttData = data;
        }
    }

    /**
     * Exposes the private field TravelTimeCalculator.linkData
     * @return
     * @throws NoSuchFieldException
     */
    public Map<Id<Link>, DataContainer> getLinkData() throws NoSuchFieldException, IllegalAccessException {
        Field linkDataField = super.getClass().getDeclaredField("linkData");
        linkDataField.setAccessible(true);
        return (Map<Id<Link>, DataContainer>) linkDataField.get(this);
    }

    /**
     * God willing, this will write the linkData to a file.
     * @param serialPath
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public void serializeLinkData(String serialPath) throws IOException, NoSuchFieldException, IllegalAccessException {
        FileOutputStream fileOut = new FileOutputStream(serialPath);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(this.getLinkData());
        out.close();
        fileOut.close();
    }

}
