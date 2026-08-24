package io.confluent.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

/**
 * Minimal replacement for the deprecated/unresolvable
 * com.github.gAmUssA:testcontainers-java-module-confluent-platform jitpack artifact.
 */
public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {

  public SchemaRegistryContainer(String version) {
    super("confluentinc/cp-schema-registry:" + version);
    withExposedPorts(8081);
  }

  public SchemaRegistryContainer withKafka(KafkaContainer kafka) {
    return withKafka(kafka.getNetwork(), kafka.getNetworkAliases().get(0) + ":9092");
  }

  public SchemaRegistryContainer withKafka(Network network, String bootstrapServers) {
    withNetwork(network);
    withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
    withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081");
    withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://" + bootstrapServers);
    return self();
  }

  public String getTarget() {
    return "http://" + getContainerIpAddress() + ":" + getMappedPort(8081);
  }

  public String getSchemaRegistryUrl() {
    return "http://" + getContainerIpAddress() + ":" + getMappedPort(8081);
  }
}
