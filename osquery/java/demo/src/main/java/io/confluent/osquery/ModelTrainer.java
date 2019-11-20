package io.confluent.osquery;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class ModelTrainer {

    public static void main(String... args) throws Exception {

        // create Options object
        Options options = new Options();

        options.addOption("s", true, "source directory for ML training");
        options.addOption("k", true, "number of topic clusters to train");
        options.addOption("e", true, "endpoint url to post the newly trained model");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        String dir = cmd.hasOption("s") ? cmd.getOptionValue("s") : ".";
        int k = cmd.hasOption("k") ? Integer.parseInt(cmd.getOptionValue("k")) : 100;
        String endpoint = cmd.hasOption("e") ? cmd.getOptionValue("e") : "http://model-server:4567/model/lates";

        Stream<String> files = Files
                .list(Paths.get(dir))
                .filter((f) -> Files.isRegularFile(f))
                .flatMap((f) -> {
                    try {
                        return Files.readAllLines(f).stream();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter((f) -> f != null);

        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add(new TokenSequence2FeatureSequence());
        SerialPipes sp = new SerialPipes(pipeList);
        InstanceList instances = new InstanceList(sp);

        StringArrayIterator sai = new StringArrayIterator(files.toArray(String[]::new));
        instances.addThruPipe(sai); // data, label, name fields

        // Model
        ParallelTopicModel model = new ParallelTopicModel(k, 1.0, 0.01);
        model.addInstances(instances);
        model.setNumThreads(4);
        model.setNumIterations(50);
        model.estimate();

        // Serialize
        call(model, endpoint);
    }

    private static void call(ParallelTopicModel model, String endpoint) {
        try {

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeObject(model);
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            StringBuffer sb = new StringBuffer();
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }

            conn.disconnect();
        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
