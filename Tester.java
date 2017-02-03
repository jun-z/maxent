package maxent;

/**
 * Created by Raymond Z on 5/16/2016.
 */

import java.util.Set;
import java.util.Iterator;
import java.util.NavigableMap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class Tester {
    public static void main(String[] args) {

        // Read model file
        String modelFile = args[0];

        InputStream is = null;

        DoccatModel m = null;

        try {
            is = new FileInputStream(new File(modelFile));
            m = new DoccatModel(is);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Score the file
        String inputFile = args[1];
        String outputFile = args[2];

        BufferedReader input = null;
        BufferedWriter output = null;

        try {
            input = new BufferedReader(new FileReader(inputFile));
            output = new BufferedWriter(new FileWriter(outputFile));

            String inLine;
            String outLine;

            while ((inLine = input.readLine()) != null) {
                String[] splitLine = inLine.split("\t");

                // Check length of this array here

                outLine = splitLine[0] + "\t";

                DocumentCategorizerME myCategorizer =
                        new DocumentCategorizerME(m);

                NavigableMap<Double, Set<String>> map =
                        (NavigableMap<Double, Set<String>>)
                                myCategorizer.sortedScoreMap(splitLine[1]);

                Iterator<Double> probabilities =
                        map.descendingKeySet().iterator();

                // Count how many classes
                int i = 1;

                iter:

                while (probabilities.hasNext()) {

                    double probability = probabilities.next();

                    Iterator<String> categories =
                            map.get(probability).iterator();

                    while (categories.hasNext()) {
                        if (i < 4) {
                            outLine = outLine + probability + "\t"
                                    + categories.next() + "\t";
                            i++;
                        }
                        else {
                            outLine = outLine + probability + "\t"
                                    + categories.next();
                            break iter;
                        }
                    }
                }
                output.write(outLine, 0, outLine.length());
                output.newLine();

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
