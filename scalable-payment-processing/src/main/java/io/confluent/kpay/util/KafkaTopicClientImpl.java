/**
 * Copyright 2018 Confluent Inc.
 * <p>
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/AGPL-3.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kpay.util;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KafkaTopicClientImpl implements KafkaTopicClient {

    private static final Logger log = LoggerFactory.getLogger(KafkaTopicClient.class);
    private static final int NUM_RETRIES = 5;
    private static final int RETRY_BACKOFF_MS = 500;
    private final AdminClient adminClient;


    private boolean isDeleteTopicEnabled = false;

    public KafkaTopicClientImpl(final AdminClient adminClient) {
        this.adminClient = adminClient;
        init();
    }

    @Override
    public void createTopic(
            final String topic,
            final int numPartitions,
            final short replicationFactor
    ) {
        createTopic(topic, numPartitions, replicationFactor, Collections.emptyMap());
    }

    @Override
    public void createTopic(
            final String topic,
            final int numPartitions,
            final short replicationFactor,
            final Map<String, String> configs
    ) {
        if (isTopicExists(topic)) {
            validateTopicProperties(topic, numPartitions, replicationFactor);
            return;
        }
        NewTopic newTopic = new NewTopic(topic, numPartitions, replicationFactor);
        newTopic.configs(configs);
        try {
            log.info("Creating topic '{}'", topic);
            RetryHelper<Void> retryHelper = new RetryHelper<>();
            retryHelper.executeWithRetries(() -> adminClient.createTopics(Collections.singleton(newTopic))
                    .all());
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Failed to guarantee existence of topic " + topic, e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                // if the topic already exists, it is most likely because another node just created it.
                // ensure that it matches the partition count and replication factor before returning
                // success
                validateTopicProperties(topic, numPartitions, replicationFactor);
                return;
            }
            throw new RuntimeException("Failed to guarantee existence of topic" + topic,
                    e);
        }
    }

    @Override
    public boolean isTopicExists(final String topic) {
        log.trace("Checking for existence of topic '{}'", topic);
        return listTopicNames().contains(topic);
    }

    @Override
    public Set<String> listTopicNames() {
        try {
            RetryHelper<Set<String>> retryHelper = new RetryHelper<>();
            return retryHelper.executeWithRetries(() -> adminClient.listTopics().names());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to retrieve Kafka Topic names", e);
        }
    }

    @Override
    public Map<String, TopicDescription> describeTopics(final Collection<String> topicNames) {
        try {
            RetryHelper<Map<String, TopicDescription>> retryHelper = new RetryHelper<>();
            return retryHelper.executeWithRetries(() -> adminClient.describeTopics(topicNames).all());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to Describe Kafka Topics", e);
        }
    }

    @Override
    public void deleteTopics(final List<String> topicsToDelete) {
        if (!isDeleteTopicEnabled) {
            log.info("Cannot delete topics since 'delete.topic.enable' is false. ");
            return;
        }
        final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topicsToDelete);
        final Map<String, KafkaFuture<Void>> results = deleteTopicsResult.values();
        List<String> failList = new ArrayList<>();

        for (final Map.Entry<String, KafkaFuture<Void>> entry : results.entrySet()) {
            try {
                entry.getValue().get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                failList.add(entry.getKey());
            }
        }
        if (!failList.isEmpty()) {
            throw new RuntimeException("Failed to clean up topics: " + failList.stream()
                    .collect(Collectors.joining(",")));
        }
    }

    @Override
    public void deleteInternalTopics(final String applicationId) {
        if (!isDeleteTopicEnabled) {
            log.warn("Cannot delete topics since 'delete.topic.enable' is false. ");
            return;
        }
        try {
            Set<String> topicNames = listTopicNames();
            List<String> internalTopics = new ArrayList<>();
            for (String topicName : topicNames) {
                if (isInternalTopic(topicName, applicationId)) {
                    internalTopics.add(topicName);
                }
            }
            if (!internalTopics.isEmpty()) {
                deleteTopics(internalTopics);
            }
        } catch (Exception e) {
            log.error("Exception while trying to clean up internal topics for application id: {}.",
                    applicationId, e
            );
        }
    }

    private void init() {
        try {
            DescribeClusterResult describeClusterResult = adminClient.describeCluster();
            List<Node> nodes = new ArrayList<>(describeClusterResult.nodes().get());
            if (!nodes.isEmpty()) {
                ConfigResource resource = new ConfigResource(
                        ConfigResource.Type.BROKER,
                        String.valueOf(nodes.get(0).id())
                );

                RetryHelper<Map<ConfigResource, Config>> retryHelper = new RetryHelper<>();
                DescribeConfigsResult
                        describeConfigsResult = adminClient.describeConfigs(Collections.singleton(resource));
                Map<ConfigResource, Config> config = retryHelper.executeWithRetries(
                        describeConfigsResult::all
                );

                this.isDeleteTopicEnabled = config.get(resource)
                        .entries()
                        .stream()
                        .anyMatch(configEntry -> configEntry.name().equalsIgnoreCase("delete.topic.enable")
                                && configEntry.value().equalsIgnoreCase("true"));


            } else {
                log.warn("No available broker found to fetch config info.");
                throw new RuntimeException("Could not fetch broker information. KSQL cannot initialize "
                        + "AdminClient.");
            }
        } catch (InterruptedException | ExecutionException ex) {
            log.error("Failed to initialize TopicClient: {}", ex.getMessage());
            throw new RuntimeException("Could not fetch broker information. KSQL cannot initialize "
                    + "AdminClient.", ex);
        }
    }

    private boolean isInternalTopic(final String topicName, String applicationId) {
        return topicName.startsWith(applicationId + "-")
                && (topicName.endsWith("-changelog") || topicName.endsWith("-repartition"));
    }

    public void close() {
        this.adminClient.close();
    }

    private void validateTopicProperties(String topic, int numPartitions, short replicationFactor) {
        Map<String, TopicDescription> topicDescriptions =
                describeTopics(Collections.singletonList(topic));
        TopicDescription topicDescription = topicDescriptions.get(topic);
        if (topicDescription.partitions().size() != numPartitions
                || topicDescription.partitions().get(0).replicas().size() < replicationFactor) {
            throw new RuntimeException(String.format(
                    "Topic '%s' does not conform to the requirements Partitions:%d v %d. Replication: %d "
                            + "v %d",
                    topic,
                    topicDescription.partitions().size(),
                    numPartitions,
                    topicDescription.partitions().get(0).replicas().size(),
                    replicationFactor
            ));
        }
//    // Topic with the partitions and replicas exists, reuse it!
//    log.debug(
//            "Did not create topic {} with {} partitions and replication-factor {} since it already "
//                    + "exists",
//            topic,
//            numPartitions,
//            replicationFactor
//    );
    }

    private static class RetryHelper<T> {
        T executeWithRetries(Supplier<KafkaFuture<T>> supplier) throws InterruptedException,
                ExecutionException {
            int retries = 0;
            Exception lastException = null;
            while (retries < NUM_RETRIES) {
                try {
                    if (retries != 0) {
                        Thread.sleep(RETRY_BACKOFF_MS);
                    }
                    return supplier.get().get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof RetriableException) {
                        retries++;
                        log.info("Retrying admin request due to retry exception. Retry no: "
                                + retries, e);
                        lastException = e;
                    } else {
                        throw e;
                    }
                }
            }
            throw new ExecutionException(lastException);
        }
    }

}
