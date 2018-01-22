package analysis.routing;


import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;

import java.io.IOException;


/**
 * Created by Andrew A. Campbell on 6/20/16.
 *
 * Listener is used to get the TripRouter created during mobsim.
 *
 */ 
public class RouterControlerListenerSerializeTestTTCalc implements StartupListener, IterationEndsListener, IterationStartsListener {
    private Scenario scenario;
    private String outPath;

    public RouterControlerListenerSerializeTestTTCalc(String outPath){this.outPath = outPath;}


    public void notifyStartup(StartupEvent event) {
        this.scenario = event.getServices().getScenario();
    }

    public void notifyIterationStarts(IterationStartsEvent event){
    }

    public void notifyIterationEnds(IterationEndsEvent event) {

        ////
        // Test the serializability of my classes
        ////
        TravelTimeCalculator ttCalc = event.getServices().getInjector().getProvider(TravelTimeCalculator.class).get();
        try {
            SerialLinkData slD = new SerialLinkData(ttCalc);
            slD.serializeData(this.outPath);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
