package io.confluent.osquery;

import cc.mallet.pipe.SerialPipes;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import io.confluent.common.Configurable;
import io.confluent.ksql.function.udaf.UdafDescription;
import io.confluent.ksql.function.udaf.UdafFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

@UdafDescription(name = "LDA", description = "scores a document")
public class AnomalyDetectionUDF implements Configurable {

    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private Map<String, ?> map;
    private Optional<ParallelTopicModel> model = Optional.empty();
    private Future<ParallelTopicModel> future;

    @UdafFactory(description = "score a document")
    public double score(final String... values) {
        String doc = StringUtils.join(values, " ");
        if(!model.isPresent()) return -1;
        ParallelTopicModel m = model.get();
        // Create a new instance named "test instance" with empty target and source fields.
        ArrayList pipeList = new ArrayList();
        SerialPipes sp = new SerialPipes(pipeList);
        InstanceList event = new InstanceList(sp);
        event.addThruPipe(new Instance(doc, null, "instance", null));

        TopicInferencer inferencer = m.getInferencer();
        double[] probabilities = inferencer.getSampledDistribution(event.get(0), 10, 1, 5);

        DoubleStream stream = Arrays.stream(probabilities);
        return stream.max().getAsDouble(); // find the max probability. we don't care which topic it belongs
    }

    @Override
    public void configure(Map<String, ?> map) {
        this.map = map;
        exec.submit(() -> subscribe(map));
    }

    private void subscribe(Map<String, ?> map) {
        Properties props = new Properties();
        String bootstrapServers = map.get("KSQL_FUNCTIONS_LDA_BOOTSTRAP_SERVERS").toString();
        String topic = map.get("KSQL_FUNCTIONS_LDA_TOPIC").toString();
        String groupid = map.get("KSQL_FUNCTIONS_LDA_GROUP_ID").toString();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupid);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        Consumer modelConsumer = new KafkaConsumer(props);
        modelConsumer.subscribe(Arrays.asList(topic));

        TopicPartition tp = new TopicPartition("lda-model", 0);
        while (true) {
            try {
                ConsumerRecords modelRecords = modelConsumer.poll(Duration.ofSeconds(2));
                if (!modelRecords.isEmpty()) {
                    Spliterator<ConsumerRecord<String, String>> splitter = Spliterators.spliteratorUnknownSize(
                            modelRecords.iterator(), 0);

                    Stream<ConsumerRecord<String, String>> stream = StreamSupport.stream(splitter, false);
                    Optional<ConsumerRecord<String, String>> record = stream.max((o1, o2) -> (int) (o1.offset() - o2.offset())); // get highest offset

                    model = deserializeModel(record.get().value());
                }
                else if (!model.isPresent()) {
                    long position = modelConsumer.position(tp);
                    modelConsumer.seek(tp, position < 1 ? position : position - 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private Optional<ParallelTopicModel> deserializeModel(String path) throws IOException, ClassNotFoundException {
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream is = connection.getInputStream();
        ObjectInputStream in = new ObjectInputStream(is);
        return Optional.of((ParallelTopicModel)in.readObject());
    }
}
