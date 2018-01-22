package utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//import java.util.stream.Collectors;

/**
 * Created by Andrew A. Campbell on 5/5/16.
 */
public class CSVWriters {

    /**
     *  This is useful when you have many arrays, all of equal length, where the n'th entry of each
     *  array belongs in the n'th row of the output csv.
     * @param values
     * @param header
     * @param sep
     * @param outPath
     * @throws IOException
     */
    public static void writeFile(ArrayList<ArrayList<Object>> values, ArrayList<Object> header,
            char sep, String outPath) throws IOException, ClassCastException {

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Attempt to cast all non-string input to strings
        ArrayList<ArrayList<String>> stringValues = new ArrayList<ArrayList<String>>();
        values.forEach(valObjs -> stringValues.add((ArrayList<String>)
                valObjs.stream().map(String::valueOf).collect(Collectors.toList())));
        //        for (ArrayList<Object> valObjs : values){
        ////            ArrayList<String> stringList = new ArrayList<String>();
        ////            for (Object o : valObjs){
        ////                stringList.add(String.valueOf(o));
        ////            }
        ////            stringValues.add(stringList);
        //            stringValues.add((ArrayList<String>) valObjs.stream().map(String::valueOf).collect(Collectors.toList()));
        //        }
        ArrayList<String> stringHeader = (ArrayList<String>) header.stream()
                .map(String::valueOf).collect(Collectors.toList());


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Write the output
        File file = new File(outPath);
        if (!file.exists()){
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bfw = new BufferedWriter(fw);
        String head = StringUtils.join(stringHeader, sep);
        bfw.write(head);
        bfw.newLine();
        // Iterate over items in each array ("rows")
        for (int i = 0; i < stringValues.get(0).size(); i++) {
            ArrayList elements = new ArrayList<>();
            // Iterate over each array ("columns")
            for (ArrayList<String> col : stringValues) {
                elements.add(col.get(i));
            }
            //            elements.add("/n");
            String line = StringUtils.join(elements, sep);
            //fw.write(line);
            bfw.write(line);
            bfw.newLine();
        }
        bfw.close();
    }

    public static <T, U> void writeFileGeneric(ArrayList<T> values, ArrayList<U> header, char sep,
                                               String outPath) throws IOException {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Attempt to cast all non-string input to strings
        ArrayList<List<String>> stringValues = new ArrayList<>();
        values.forEach(valObjs -> stringValues.add((ArrayList<String>)
                ((ArrayList)valObjs).stream().map(String::valueOf).collect(Collectors.toList())));
        ArrayList<String> stringHeader = (ArrayList<String>) header.stream()
                .map(String::valueOf).collect(Collectors.toList());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Write the output
        File file = new File(outPath);
        if (!file.exists()){
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bfw = new BufferedWriter(fw);
        String head = StringUtils.join(stringHeader, sep);
        bfw.write(head);
        bfw.newLine();
        // Iterate over items in each array ("rows")
        for (int i = 0; i < stringValues.get(0).size(); i++) {
            ArrayList elements = new ArrayList<>();
            // Iterate over each array ("columns")
            for (List<String> col : stringValues) {
                elements.add(col.get(i));
            }
            //            elements.add("/n");
            String line = StringUtils.join(elements, sep);
            //fw.write(line);
            bfw.write(line);
            bfw.newLine();
        }
        bfw.close();

    }


    public static void writeFileJDK7(ArrayList<ArrayList<String>> values, ArrayList<String> header,
            char sep, String outPath) throws IOException, ClassCastException {

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Write the output
        File file = new File(outPath);
        if (!file.exists()){
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bfw = new BufferedWriter(fw);
        String head = StringUtils.join(header, sep);
        bfw.write(head);
        bfw.newLine();
        // Iterate over items in each array ("rows")
        for (int i = 0; i < values.get(0).size(); i++) {
            ArrayList elements = new ArrayList<>();
            // Iterate over each array ("columns")
            for (ArrayList<String> col : values) {
                elements.add(col.get(i));
            }
            //elements.add("/n");
            String line = StringUtils.join(elements, sep);
            //fw.write(line);
            bfw.write(line);
            bfw.newLine();
        }
        bfw.close();
    }
}
