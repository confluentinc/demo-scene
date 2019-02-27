package io.confluent.demo.naive;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class StreamProcessingUsingKafkaStreams {

  public static void main(String[] args) {
    final StreamsBuilder streamsBuilder = new StreamsBuilder();
    final KStream<String, Long> stream = streamsBuilder.stream(Arrays.asList("A", "B"));

    // actual work
    stream.groupByKey()
        .count()
        .toStream()
        .to("group-by-counts",
            Produced.with(Serdes.String(), Serdes.Long()));

    //----

    final Topology topology = streamsBuilder.build();
    final KafkaStreams kafkaStreams = new KafkaStreams(topology, streamsProperties());
    kafkaStreams.start();
  }

  private static Properties streamsProperties() {
    final Properties streamsProperties = new Properties();
    return streamsProperties;
  }
}
