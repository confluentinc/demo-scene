package io.confluent.devrel.product;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.support.TestPropertyProvider;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

abstract class BaseProductTest implements TestPropertyProvider {

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native")
            .asCompatibleSubstituteFor("apache/kafka"));

    @Override
    public @NonNull Map<String, String> getProperties() {
        if (!kafka.isRunning()) {
            kafka.start();
        }

        return Map.of(
                "kafka.bootstrap.servers", kafka.getBootstrapServers()
        );
    }

}
