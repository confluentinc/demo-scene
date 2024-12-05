package io.confluent.devrel.order;

import io.confluent.devrel.event.OrderChangeEvent;
import io.confluent.devrel.event.OrderChangeEventType;
import io.confluent.devrel.model.OrderItem;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.specification.RequestSpecification;
import net.datafaker.Faker;
import net.datafaker.providers.base.Options;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(transactional = false, propertySources = {"classpath:application-test.yml"}, environments = {"test"})
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderControllerTest implements TestPropertyProvider {

    static final Network network = Network.newNetwork();

    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"))
            .withNetwork(network);

    GenericContainer<?> schemaRegistry = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:7.7.0"))
            .withNetwork(network)
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                    String.format("PLAINTEXT://%s:9093", kafka.getNetworkAliases().get(0)))
            .withNetworkAliases("schema-registry")
            .waitingFor(Wait.forHttp("/subjects").forStatusCode(200));

    String schemaRegistryUrl;

    Properties consumerProperties;
    KafkaConsumer<Long, OrderChangeEvent> eventConsumer;

    @Override
    public @NonNull Map<String, String> getProperties() {
        if (!kafka.isRunning()) {
            kafka.start();
        }

        if (!schemaRegistry.isRunning()) {
            schemaRegistry.start();
        }

        schemaRegistryUrl = String.format("http://%s:%d", schemaRegistry.getHost(), schemaRegistry.getMappedPort(8081));

        consumerProperties = new Properties(){{
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
            put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
            put("specific.avro.reader", "true");
        }};

        eventConsumer = new KafkaConsumer<Long, OrderChangeEvent>(consumerProperties);

        return Map.of(
                "kafka.bootstrap.servers", kafka.getBootstrapServers(),
                "schema.registry.url", schemaRegistryUrl,
                "kafka.consumers.order-changes.specific.avro.reader", "true",
                "kafka.consumers.order-changes.schema.registry.url", schemaRegistryUrl,
                "kafka.producers.order-changes.schema.registry.url", schemaRegistryUrl
        );
    }

    /**
     * This test validates we get a response from a PUT request.
     *
     * @param spec
     */
    @Test
    public void testPutChangeEvent(RequestSpecification spec) {

        Faker faker = new Faker();
        Options types = faker.options();

        final long orderId = faker.number().numberBetween(1L, 999999L);
        OrderItem item = OrderItem.newBuilder()
                .setOrderId(orderId)
                .setProductId(UUID.randomUUID().toString())
                .setDescription(faker.lorem().word())
                .setQuantity(1)
                .setUnitPrice(faker.number().randomDouble(2, 9, 100))
                .build();

        OrderChangeEvent event = OrderChangeEvent.newBuilder()
                .setOrderId(orderId)
                .setEventType(types.option(OrderChangeEventType.class))
                .setItem(item)
                .build();

        eventConsumer.subscribe(List.of("order-changes-avro"));

        spec.given()
                .header("Content-Type", "application/json")
                .body(toJson(event))
                .when()
                .put("/orders/change/{id}", orderId)
                .then()
                .statusCode(201);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            // poll kafka
            ConsumerRecords<Long, OrderChangeEvent> records = eventConsumer.poll(Duration.ofSeconds(2));
            // assert there is ONE record from the `order-changes-avro` topic with a key == the orderId value we sent.
            assertEquals(1L, StreamSupport.stream(
                            records.records("order-changes-avro").spliterator(), false)
                    .filter( r -> r.key() == orderId).count());
        });
    }


    private static <T extends SpecificRecord> String toJson(T avroObject) {
        DatumWriter<T> writer = new SpecificDatumWriter<>(avroObject.getSchema());
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            Encoder jsonEncoder = EncoderFactory.get().jsonEncoder(avroObject.getSchema(), stream);
            writer.write(avroObject, jsonEncoder);
            jsonEncoder.flush();
            return stream.toString();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }
}
