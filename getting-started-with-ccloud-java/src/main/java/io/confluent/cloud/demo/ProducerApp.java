package io.confluent.cloud.demo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import io.confluent.cloud.demo.domain.SensorReadingImpl.SensorReading;
import io.confluent.cloud.demo.domain.SensorReadingImpl.SensorReading.Device;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;

import static io.confluent.cloud.demo.utils.KafkaUtils.TOPIC;
import static io.confluent.cloud.demo.utils.KafkaUtils.createTopic;
import static io.confluent.cloud.demo.utils.KafkaUtils.getConfigs;

public class ProducerApp {

    private void run(Properties configs) {

        // Add the serializer configuration for the key and value
        configs.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName());
        configs.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            KafkaProtobufSerializer.class.getName());

        try (KafkaProducer<String, SensorReading> producer = new KafkaProducer<>(configs)) {

            for (;;) {

                // Randomly pick a device from the list
                // and use its identifier as record key
                int index = RANDOM.nextInt(DEVICES.size()-1);
                Device device = DEVICES.get(index);
                String recordKey = device.getDeviceID();

                // Create a new record based on the sensor
                // data from the device that was picked.
                ProducerRecord<String, SensorReading> record =
                    new ProducerRecord<>(TOPIC, recordKey,
                        SensorReading.newBuilder()
                            .setDevice(device)
                            .setDateTime(new Date().getTime())
                            .setReading(RANDOM.nextDouble())
                            .build());

                // Write the sensor data to the topic's partition
                producer.send(record, (metadata, exception) -> {
                    System.out.println(String.format(
                        "Reading sent to partition %d with offset %d",
                        metadata.partition(), metadata.offset()));
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }

            }

        }

    }

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static final List<Device> DEVICES = Arrays.asList(
        Device.newBuilder()
            .setDeviceID(UUID.randomUUID().toString())
            .setEnabled(RANDOM.nextBoolean())
            .build(),
        Device.newBuilder()
            .setDeviceID(UUID.randomUUID().toString())
            .setEnabled(RANDOM.nextBoolean())
            .build(),
        Device.newBuilder()
            .setDeviceID(UUID.randomUUID().toString())
            .setEnabled(RANDOM.nextBoolean())
            .build(),
        Device.newBuilder()
            .setDeviceID(UUID.randomUUID().toString())
            .setEnabled(RANDOM.nextBoolean())
            .build(),
        Device.newBuilder()
            .setDeviceID(UUID.randomUUID().toString())
            .setEnabled(RANDOM.nextBoolean())
            .build()
    );

    public static void main(String[] args) {
        createTopic(TOPIC, 4, (short) 3);
        new ProducerApp().run(getConfigs());
    }

}
