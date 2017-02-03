package maxent;

/**
 * Created by Raymond Z on 5/16/2016.
 */

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;

import java.util.Properties;

import opennlp.tools.tokenize.WhitespaceTokenizer;

import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.doccat.BagOfWordsFeatureGenerator;
import opennlp.tools.doccat.NGramFeatureGenerator;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DoccatFactory;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

public class Trainer {
    public static void main(String[] args) {

        // Train the model
        String propsPath = args[0];

        InputStream propsStream = null;
        InputStream paramsStream = null;

        String pathIn = args[1];

        File fileIn = new File(pathIn);

        DoccatModel model = null;

        InputStreamFactory dataIn;


        try {
            // Read data in
            dataIn = new MarkableFileInputStreamFactory(fileIn);

            ObjectStream<String> lineStream =
                    new PlainTextByLineStream(dataIn, "UTF-8");

            ObjectStream<DocumentSample> sampleStream =
                    new DocumentSampleStream(lineStream);

            // Tokenizer
            WhitespaceTokenizer tokenizer = WhitespaceTokenizer.INSTANCE;

            // Read model parameters
            paramsStream = new FileInputStream(new File(propsPath));

            // Set training parameters
            TrainingParameters mlParams = new TrainingParameters(paramsStream);

            // Read properties
            propsStream = new FileInputStream(new File(propsPath));

            Properties properties = new Properties();

            properties.load(propsStream);

            // Feature generator
            FeatureGenerator[] generator;

            if ("true".equals(properties.getProperty("Bigram"))) {
                generator = new FeatureGenerator[2];

                generator[0] = new BagOfWordsFeatureGenerator();
                generator[1] = new NGramFeatureGenerator();
            }
            else {
                generator = new FeatureGenerator[1];

                generator[0] = new BagOfWordsFeatureGenerator();
            }

            DoccatFactory factory =
                    new DoccatFactory(tokenizer, generator);

            // Train
            model =
                    DocumentCategorizerME.train("en", sampleStream, mlParams, factory);
        }
        catch (IOException e) {
            // Failed to read or parse training data, training failed
            e.printStackTrace();
        }
        finally {
            if (paramsStream != null) {
                try {
                    paramsStream.close();
                }
                catch (IOException e) {
                    // Not an issue, training already finished.
                    // The exception should be logged and investigated
                    // if part of a production system.
                    e.printStackTrace();
                }
            }
            if (propsStream != null) {
                try {
                    propsStream.close();
                }
                catch (IOException e) {
                    // Not an issue, training already finished.
                    // The exception should be logged and investigated
                    // if part of a production system.
                    e.printStackTrace();
                }
            }

        }

        // Save the model
        String pathOut = args[2];

        File modelFile = new File(pathOut);

        OutputStream modelOut = null;

        try {
            modelOut =
                    new BufferedOutputStream(new FileOutputStream(modelFile));

            model.serialize(modelOut);
        }
        catch (IOException e) {
            // Failed to save model
            e.printStackTrace();
        }
        finally {
            if (modelOut != null) {
                try {
                    modelOut.close();
                }
                catch (IOException e) {
                    // Failed to correctly save model.
                    // Written model might be invalid.
                    e.printStackTrace();
                }
            }
        }
    }
}
