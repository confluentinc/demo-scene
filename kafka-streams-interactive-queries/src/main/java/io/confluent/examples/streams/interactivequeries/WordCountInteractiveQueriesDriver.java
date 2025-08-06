/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.examples.streams.interactivequeries;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * This is a sample driver for the {@link WordCountInteractiveQueriesExample}.
 * To run this driver please first refer to the instructions in {@link WordCountInteractiveQueriesExample}.
 * You can then run this class directly in your IDE or via the command line.
 *
 * To run via the command line you might want to package as a fatjar first. Please refer to:
 * <a href='https://github.com/confluentinc/kafka-streams-examples#packaging-and-running'>Packaging</a>
 *
 * Once packaged you can then run:
 * <pre>
 * {@code
 * $ java -cp target/kafka-streams-examples-8.0.0-standalone.jar io.confluent.examples.streams.interactivequeries.WordCountInteractiveQueriesDriver
 * }
 * </pre>
 * You should terminate with Ctrl-C
 */
public class WordCountInteractiveQueriesDriver {

  public static void main(final String [] args) throws Exception {
    final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
    final List<String> inputValues = Arrays.asList("hello world",
                                                   "all streams lead to kafka",
                                                   "streams",
                                                   "kafka streams",
                                                   "the cat in the hat",
                                                   "green eggs and ham",
                                                   "that sam i am",
                                                   "up the creek without a paddle",
                                                   "run forest run",
                                                   "a tank full of gas",
                                                   "eat sleep rave repeat",
                                                   "one jolly sailor",
                                                   "king of the world");

    final Properties producerConfig = new Properties();
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
    producerConfig.put(ProducerConfig.RETRIES_CONFIG, 1);
    producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    final KafkaProducer<String, String>
        producer =
        new KafkaProducer<>(producerConfig, new StringSerializer(), new StringSerializer());

    // Always begin with writing the first line to the topic so that the REST API example calls in
    // our step-by-step instructions always work right from the start (otherwise users may run into
    // HTTP 404 errors when querying the latest value for a key, for example, until the right input
    // data was sent to the topic).
    producer.send(new ProducerRecord<>(WordCountInteractiveQueriesExample.TEXT_LINES_TOPIC,
                                       inputValues.get(0), inputValues.get(0)));

    // every 500 milliseconds produce one of the lines of text from inputValues to the
    // TextLinesTopic
    final Random random = new Random();
    while (true) {
      final int i = random.nextInt(inputValues.size());
      producer.send(new ProducerRecord<>(WordCountInteractiveQueriesExample.TEXT_LINES_TOPIC,
                                         inputValues.get(i), inputValues.get(i)));
      Thread.sleep(500L);
    }
  }

}
