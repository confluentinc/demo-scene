package io.confluent.osquery;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;
import spark.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

import static spark.Spark.*;


/**
 * Model Server
 *
 */
public class ModelServer
{
    public static final Stack<Pair<String, ParallelTopicModel>> stack = new Stack<>();

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("s", true, "source directory for ML training");
        options.addOption("k", true, "number of topic clusters to train");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        String dir = cmd.hasOption("s") ? cmd.getOptionValue("s") : "logs";
        int k = cmd.hasOption("k") ? Integer.parseInt(cmd.getOptionValue("k")) : 100;

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.schedule(() -> train(dir, k), 1, TimeUnit.MINUTES);

        initExceptionHandler((e) -> {
            e.printStackTrace();
            System.out.println("Uh-oh");
        });

        /**
         *  http://localhost:4567/status
         */
        get("/status", (req, res) -> !stack.isEmpty()?  stack.peek().getKey() : "empty");

        post("/model/latest/score", (req, res) -> score(req));

        delete("/model/latest", (request, response) -> {
            // rollback a model
            String lastModel = stack.pop().getKey();
            return "removing "+lastModel;
        });
    }

    private static void train(String dir, int k) {
        try {
            stack.push(ModelTrainer.train(dir, k));
            while(stack.size() > 5) stack.remove(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double score(Request req) throws Exception {

        String doc = req.body();
        if(stack.isEmpty()) throw new IllegalStateException("model not available");

        try {
            ParallelTopicModel model = stack.peek().getValue();

            ArrayList<Pipe> pipeList = new ArrayList<>();
            String regex = "\\b(\\w*[^\\d][\\w\\.\\:]*\\w)\\b";
            pipeList.add(new CharSequence2TokenSequence());
            pipeList.add(new TokenSequence2FeatureSequence());
            SerialPipes sp = new SerialPipes(pipeList);
            InstanceList event = new InstanceList(sp);
            event.addThruPipe(new Instance(doc, null, "instance", null));

            TopicInferencer inferencer = model.getInferencer();
            double[] probabilities = inferencer.getSampledDistribution(event.get(0), 50, 1, 5);

            DoubleStream stream = Arrays.stream(probabilities);
            double score = stream.max().getAsDouble();
            return score; // find the max probability. we don't care which topic it belongs
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception((e));
        }
    }
}
