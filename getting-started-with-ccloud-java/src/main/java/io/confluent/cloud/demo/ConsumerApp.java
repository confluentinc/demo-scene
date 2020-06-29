package io.confluent.cloud.demo;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.confluent.cloud.demo.domain.SensorReadingImpl.SensorReading;
import io.confluent.cloud.demo.domain.SensorReadingImpl.SensorReading.Device;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;

import static io.confluent.cloud.demo.utils.KafkaUtils.TOPIC;
import static io.confluent.cloud.demo.utils.KafkaUtils.createTopic;
import static io.confluent.cloud.demo.utils.KafkaUtils.getConfigs;

public class ConsumerApp {

    private void run(Properties configs) {

        // Add the deserializer configuration for the key and value
        configs.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName());
        configs.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            KafkaProtobufDeserializer.class.getName());

        // Instructs the deserializer to perform deserialization using the
        // specific value type instead of using the 'DynamicMessage' type.
        configs.setProperty(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE,
            SensorReading.class.getName());

        // Set other standard properties for the Kafka consumer
        configs.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "java-consumer");

        try (KafkaConsumer<String, SensorReading> consumer = new KafkaConsumer<>(configs)) {

            consumer.subscribe(Arrays.asList(TOPIC));

            for (;;) {

                ConsumerRecords<String, SensorReading> records =
                    consumer.poll(Duration.ofSeconds(Integer.MAX_VALUE));

                for (ConsumerRecord<String, SensorReading> record : records) {
                    SensorReading sensorReading = record.value();
                    Device device = sensorReading.getDevice();
                    StringBuilder sb = new StringBuilder();
                    sb.append("SensorReading[device=").append(device.getDeviceID());
                    sb.append(", dateTime=").append(sensorReading.getDateTime());
                    sb.append(", reading=").append(sensorReading.getReading());
                    sb.append("]\n");
                    System.out.println(sb.toString());
                }

            }

        }

    }

    public static void main(String[] args) {
        createTopic(TOPIC, 4, (short) 3);
        new ConsumerApp().run(getConfigs());
    }

}
