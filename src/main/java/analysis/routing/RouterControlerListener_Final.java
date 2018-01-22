package analysis.routing;


import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.router.TripRouter;


/**
 * Created by Andrew A. Campbell on 6/20/16.
 *
 * Listener is used to get the TripRouter created during mobsim.
 *
 */ 
public class RouterControlerListener_Final implements StartupListener, IterationEndsListener, IterationStartsListener {
    private Scenario scenario;
    private TripRouter tripRouter;


    public void notifyStartup(StartupEvent event) {
        this.scenario = event.getServices().getScenario();
    }

    public void notifyIterationStarts(IterationStartsEvent event){
    }

    public void notifyIterationEnds(IterationEndsEvent event) {

        ////
        // Get the TripRouter created by the run
        ////
        this.tripRouter = event.getServices().getInjector().getProvider(TripRouter.class).get();
    }

    public TripRouter getRouter(){
        return this.tripRouter;
    }


}
