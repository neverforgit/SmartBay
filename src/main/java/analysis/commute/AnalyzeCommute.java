package analysis.commute;

import java.io.IOException;

/**
 * Created by Andrew A. Campbell on 9/29/16.
 *
 * System args:
 * 0 - path to events file
 * 1 - path to config file
 * 2 - path to output directory
 */
public class AnalyzeCommute  {
    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        CommuteAnalyzer analyzer = new CommuteAnalyzer(args[0], args[1]);
        ////
        // For a test run using the equil scnario
        ////
//        analyzer.getCommuteHandler().setFreewaySpeed(20);
//        analyzer.getCommuteHandler().setCongestionCutOffSpeed(20);
//        analyzer.getCommuteHandler().setHome("h");
//        analyzer.getCommuteHandler().setWork("w");
        ////
        // For a full run
        ////
        analyzer.getCommuteHandler().setHome("Home");
        analyzer.getCommuteHandler().setWork("Work");
        analyzer.run();
        System.out.println("Time to run: " + String.valueOf((System.currentTimeMillis() - t0)/1000.0));
        long t1 = System.currentTimeMillis();
        try {
            analyzer.write(args[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Time to write: " + String.valueOf((System.currentTimeMillis() - t1)/1000.0));
    }
}
