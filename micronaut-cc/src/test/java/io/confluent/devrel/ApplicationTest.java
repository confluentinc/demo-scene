package io.confluent.devrel;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@MicronautTest(transactional = false, propertySources = {"classpath:application-test.yml"})
@Property(name = "datasources.default.driver-class-name", value = "org.testcontainers.jdbc.ContainerDatabaseDriver")
@Property(name = "datasources.default.url", value = "jdbc:tc:mysql:8:///db")
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest implements TestPropertyProvider {

    @Inject
    EmbeddedApplication<?> application;

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

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

}
