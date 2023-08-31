/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package clients;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import serde.JSONSerde;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class GitHubPrRatio {

    static class MyProcessorSupplier implements ProcessorSupplier<JsonNode, JsonNode, JsonNode, JsonNode> {

        @Override
        public Processor<JsonNode, JsonNode, JsonNode, JsonNode> get() {
            return new Processor<>() {

                private KeyValueStore<JsonNode, JsonNode> kvStore;
                private String jsonKey = "{ \"key\" : \"apache/kafka\" } ";
                private JsonNode jsonNodeKey = JsonNodeFactory.instance.textNode(jsonKey);
                private String openString = "{\"open\": \"0\"}";
                private String closedString = "{\"closed\": \"0\"}";
                private JsonNode openNode = JsonNodeFactory.instance.textNode(openString);
                private JsonNode closedNode = JsonNodeFactory.instance.textNode(closedString);

                @Override
                public void init(final ProcessorContext<JsonNode, JsonNode> context) {

                    this.kvStore = context.getStateStore("PRState");

                    context.schedule(Duration.ofSeconds(1), PunctuationType.STREAM_TIME, timestamp -> {

                        try (final KeyValueIterator<JsonNode, JsonNode> iter = kvStore.all()) {
                            System.out.println("----------- " + timestamp + " ----------- ");
                            while (iter.hasNext()) {
                                final KeyValue<JsonNode, JsonNode> entry = iter.next();
                                System.out.println("[" + entry.key + ", " + entry.value + "]");
                                context.forward(new Record<>(entry.key, entry.value, timestamp));
                            }
                        }
                    });

                }

                @Override
                public void process(final Record<JsonNode, JsonNode> record) {

                    System.out.println("process started");
                    JsonNode prStateVal = record.value().findValue("state");
                    JsonNode retrievedVal = kvStore.get(jsonNodeKey);

                    if (retrievedVal == null) {
                        retrievedVal = JsonNodeFactory.instance.objectNode();
                        ((ObjectNode) retrievedVal).put("open", 0);
                        ((ObjectNode) retrievedVal).put("closed", 0);
                    }

                    //something happening with open value to make process halt, try stepping in
                    if (prStateVal.asText().contains("open")) {
                        JsonNode countNodeOpen = retrievedVal.get("open") != null ? retrievedVal.get("open") : openNode;
                        ((ObjectNode) retrievedVal).put("open", countNodeOpen.asInt() + 1);

                    } else if (prStateVal.asText().contains("closed")) {
                        JsonNode countNodeClosed = retrievedVal.get("closed") != null ? retrievedVal.get("closed") : closedNode;
                        ((ObjectNode) retrievedVal).put("closed", countNodeClosed.asInt() + 1);
                    }

                    this.kvStore.put(jsonNodeKey, retrievedVal);

                    System.out.println(retrievedVal);
                }

                public void close() {
                }
            };


        }

        final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);

        @Override
        public Set<StoreBuilder<?>> stores() {
            return Collections.singleton(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore("PRState"), Serdes.serdeFrom(jsonSerializer, jsonDeserializer), jsonSerde));
        }
    }

    public static Properties loadEnvProperties(String fileName) throws IOException {
        final Properties allProps = new Properties();
        try (final FileInputStream input = new FileInputStream(fileName)) {
            allProps.load(input);
        }
        return allProps;
    }

    public static void main(final String[] args) throws Exception {
        final Properties props = GitHubPrRatio.loadEnvProperties("get-started.properties");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-github-streams-app");
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
        final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
        final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Integer> integerSerde = Serdes.Integer();
        final StreamsBuilder builder = new StreamsBuilder();

        builder.<JsonNode, JsonNode>stream("github-pull_requests", Consumed.with(jsonSerde, jsonSerde)).process(new MyProcessorSupplier()).to("state", Produced.with(jsonSerde, jsonSerde));

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-pipe-shutdown-hook") {
            @Override
            public void run() {
                streams.close(Duration.ofSeconds(5));
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}