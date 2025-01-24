package io.confluent.devrel.datacontracts.shared

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

abstract class BaseConsumer<Key, Value>(propOverrides: Map<String, Any> = mapOf<String, Any>()) {

    companion object {
        val logger = LoggerFactory.getLogger(BaseConsumer::class.java)

        fun getConsumerProperties(propOverrides: Map<String, Any>): Properties {
            val props = Properties()
            props.load(this::class.java.classLoader.getResourceAsStream("confluent.properties"))
            props.put("specific.avro.reader", "true")
            props.putAll(propOverrides)
            return props
        }
    }

    val kafkaConsumer: KafkaConsumer<Key, Value> = KafkaConsumer(getConsumerProperties(propOverrides))

    fun start(topics: List<String>) {
        kafkaConsumer.subscribe(topics)
        while (true) {
            val records = kafkaConsumer.poll(Duration.ofSeconds(5))
            for (record in records) {
                logger.trace("Record from ${record.topic()}, ${record.partition()}, ${record.offset()}")
                consumeRecord(record.key(), record.value())
            }
        }
    }

    abstract fun consumeRecord(key: Key, value: Value)

    fun close() {
        kafkaConsumer.close()
    }
}