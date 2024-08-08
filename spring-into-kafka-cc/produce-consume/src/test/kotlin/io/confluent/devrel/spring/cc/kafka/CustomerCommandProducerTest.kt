package io.confluent.devrel.spring.cc.kafka

import io.confluent.devrel.spring.command.CustomerAction
import io.confluent.devrel.spring.command.CustomerCommand
import io.confluent.devrel.spring.model.Address
import io.confluent.devrel.spring.model.Customer
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = [CustomerCommandKafka.topicName], brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
class CustomerCommandProducerTest(@Autowired private val embeddedKafkaBroker: EmbeddedKafkaBroker) {

    private val customerCommandProducer: CustomerCommandProducer by lazy {
        embeddedKafkaBroker.afterPropertiesSet()

        val producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker)
        producerProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producerProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        producerProps[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://test"
        val producerFactory = DefaultKafkaProducerFactory<String, CustomerCommand>(producerProps)
        val template = KafkaTemplate(producerFactory)

        CustomerCommandProducer(template)
    }

    private val consumerContainer: KafkaMessageListenerContainer<String, CustomerCommand> by lazy {
        val consumerProps = KafkaTestUtils.consumerProps("test_group", "true", embeddedKafkaBroker)
        consumerProps[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = "mock://test"
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java
        consumerProps["specific.avro.reader"] = true
        val consumerFactory = DefaultKafkaConsumerFactory<String, CustomerCommand>(consumerProps)
        val containerProps = ContainerProperties(CustomerCommandKafka.topicName)
        KafkaMessageListenerContainer(consumerFactory, containerProps)
    }

    private val records = LinkedBlockingQueue<CustomerCommand>()

    @BeforeEach
    fun setup() {
        // Set up and start the container before sending messages
        consumerContainer.setupMessageListener(MessageListener<String, CustomerCommand> { record -> records.add(record.value()) })
        consumerContainer.start()
    }

    @Test
    fun sendCustomerCommand() {

        val customer = Customer.newBuilder()
                .setId("1234")
                .setEmail("me@you.com")
                .setFirstName("Jay")
                .setLastName("Kreps")
                .setDob("01/01/1970")
                .setMailingAddress(Address.newBuilder()
                    .setAddress1("42 Wallaby Way")
                    .setCity("Mountain View")
                    .setState("CA")
                    .setPostalCode("90210")
                    .build())
                .build()

        customerCommandProducer.sendCustomerCommand(customer, CustomerAction.ADD)

        val received = records.poll(5, TimeUnit.SECONDS)

        assertEquals(CustomerAction.ADD, received.getAction())
        assertEquals(customer, received.getCustomer())
    }

    @AfterEach
    fun teardown() {
        consumerContainer.stop()
    }
}