package io.confluent.osquery;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * You should run this trainer as a separate process and
 * not invoke it from the model server so that you can
 * scale the model serving and training separately.
 */
public class ModelTrainer {

    public static Pair<String, ParallelTopicModel> train(String dir, int k) throws IOException {
        String[] docs = Files
                .list(Paths.get(dir))
                .parallel()
                .flatMap((f) -> {
                    try {
                        return Files.readAllLines(f).stream();
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter((f) -> f != null)
                .map((string) -> {
                    List<String> tokes = new ArrayList<>();
                    try{
                        Reader reader = new StringReader(string);
                        JsonReader jsonReader = Json.createReader(reader);
                        JsonObject doc = jsonReader.readObject();
                        JsonObject columns = doc.getJsonObject("COLUMNS");
                        Stream<String> values = columns.values().parallelStream()
                                .map((s) -> ((JsonString)s).getString().toLowerCase().trim())
                                .filter((s) -> !NumberUtils.isCreatable(s))
                                .filter((s) -> !s.isEmpty());
                        tokes.addAll(values.collect(Collectors.toList()));
                        tokes.add(doc.getString("HOSTIDENTIFIER"));
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }

                    return tokes.stream().collect(Collectors.joining(" "));
                })
                .filter((s) -> s != null)
                .toArray(String[]::new);

        ArrayList<Pipe> pipeList = new ArrayList<>();
        String regex = "\\b(\\w*[^\\d][\\w\\.\\:]*\\w)\\ b";
        pipeList.add(new CharSequence2TokenSequence());
        pipeList.add(new TokenSequence2FeatureSequence());
        SerialPipes sp = new SerialPipes(pipeList);
        InstanceList instances = new InstanceList(sp);

        StringArrayIterator sai = new StringArrayIterator(docs);
        instances.addThruPipe(sai); // data, label, name fields

        // Model
        ParallelTopicModel model = new ParallelTopicModel(k, 2.0, 0.01);
        model.addInstances(instances);
        model.setNumThreads(4);
        model.setNumIterations(50);
        model.estimate();

        // Serialize
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = sdf.format(new Date(System.currentTimeMillis()));

        return Pair.of(date, model);
    }

    public static void main(String... args) throws IOException {
        String doc = "foo bar";

        Pair<String, ParallelTopicModel> m = train("../../cp/logs/", 100);
        // LDA model
        ParallelTopicModel model = m.getValue();

        ArrayList<Pipe> pipeList = new ArrayList<>();
        pipeList.add(new CharSequence2TokenSequence());
        pipeList.add(new TokenSequence2FeatureSequence());
        SerialPipes sp = new SerialPipes(pipeList);
        InstanceList event = new InstanceList(sp);
        event.addThruPipe(new Instance(doc, null, "instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] probabilities = inferencer.getSampledDistribution(event.get(0), 50, 1, 5);

        DoubleStream stream = Arrays.stream(probabilities);
        System.out.println(stream.max().getAsDouble()); // find the max probability. we don't care which topic it belongs
    }
}
