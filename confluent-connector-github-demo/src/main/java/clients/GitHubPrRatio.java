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
import model.GitHubPRInfo;
import model.GitHubPRStateCounter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serde.StreamsSerde;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class GitHubPrRatio {
      private static final Logger LOG = LoggerFactory.getLogger(GitHubPrRatio.class);
    static final Serde<GitHubPRStateCounter> prStateCounterSerde = StreamsSerde.serdeFor(GitHubPRStateCounter.class);
    final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
    final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
    final Serde<JsonNode> jsonNodeSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);
    static final String STORE_NAME = "pr-state";
    static final String NO_STATE = "no-state";
    static final String INPUT_TOPIC = "github-pull_requests";
    static final String OUTPUT_TOPIC = "github-pull_requests";

    public Topology topology(Properties properties) {

        final ValueMapper<JsonNode, GitHubPRInfo> valueMapper = jsonNode -> {
            JsonNode prStateVal = jsonNode.findValue("state");
            JsonNode prCreatedAtVal = jsonNode.findValue("created_at");
            JsonNode prClosedAtVal = jsonNode.findValue("closed_at");
            JsonNode prNumberVal = jsonNode.findValue("number");
            String state = prStateVal != null ? prStateVal.asText() : NO_STATE;
            String createdAt = prCreatedAtVal != null ? prCreatedAtVal.asText() : "no-created-at";
            String closedAt = prClosedAtVal != null ? prClosedAtVal.asText() : "no-closed-at";
            int prNumber = prNumberVal != null ? prNumberVal.asInt() : Integer.MIN_VALUE;

            return new GitHubPRInfo(state, createdAt, closedAt, prNumber);
        };
        final StreamsBuilder builder = new StreamsBuilder();

        builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), jsonNodeSerde))
                .peek((key, value) -> LOG.info("Incoming key[{}] value[{}]", key, value))
                .mapValues(valueMapper)
                .process(new MyProcessorSupplier())
                .peek((key, value) -> LOG.info("Outgoing value key[{}] value[{}]", key, value))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), prStateCounterSerde));

        return builder.build(properties);
    }

    public static Properties loadEnvProperties(String fileName) throws IOException {
        final Properties allProps = new Properties();
        try (final FileInputStream input = new FileInputStream(fileName)) {
            allProps.load(input);
        }
        return allProps;
    }

    static class MyProcessorSupplier implements ProcessorSupplier<String, GitHubPRInfo, String, GitHubPRStateCounter> {

        static final String STORE_KEY = "state-counter";

        @Override
        public Processor<String, GitHubPRInfo, String, GitHubPRStateCounter> get() {
            return new Processor<>() {
                private KeyValueStore<String, GitHubPRStateCounter> kvStore;

                @Override
                public void init(final ProcessorContext<String, GitHubPRStateCounter> context) {
                    this.kvStore = context.getStateStore(STORE_NAME);

                    context.schedule(Duration.ofSeconds(1), PunctuationType.STREAM_TIME, timestamp -> {
                        GitHubPRStateCounter entry = kvStore.get(STORE_KEY);
                        System.out.printf("Store value %s%n", entry);
                        context.forward(new Record<>("pr", entry, timestamp));
                    });
                }

                @Override
                public void process(final Record<String, GitHubPRInfo> record) {
                    GitHubPRInfo prInfo = record.value();
                    if(!prInfo.state().equals(NO_STATE)) {
                        GitHubPRStateCounter stateCounter = kvStore.get(STORE_KEY);
                        if (stateCounter == null) {
                            stateCounter = new GitHubPRStateCounter();
                        }
                        if (prInfo.state().equalsIgnoreCase("open")) {
                            stateCounter.setOpen(stateCounter.getOpen() + 1);
                        } else {
                            stateCounter.setClosed(stateCounter.getClosed() + 1);
                        }
                        kvStore.put(STORE_KEY, stateCounter);
                    }
                }
            };
        }

        @Override
        public Set<StoreBuilder<?>> stores() {
            return Collections.singleton(Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(STORE_NAME), Serdes.String(), prStateCounterSerde));
        }
    }

    public static void main(final String[] args) throws Exception {
        final Properties props = GitHubPrRatio.loadEnvProperties("get-started.properties");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-github-87");
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        GitHubPrRatio gitHubPrRatio = new GitHubPrRatio();

        final KafkaStreams streams = new KafkaStreams(gitHubPrRatio.topology(props), props);
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close(Duration.ofSeconds(5));
            latch.countDown();
        }));

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