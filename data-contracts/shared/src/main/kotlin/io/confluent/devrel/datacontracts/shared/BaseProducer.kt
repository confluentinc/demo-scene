package io.confluent.devrel.datacontracts.shared

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Future

abstract class BaseProducer<Key, Value>(propOverrides: Map<String, Any> = mapOf<String, Any>()) {

    companion object {
        fun getProducerProperties(propOverrides: Map<String, Any>): Properties {
            val props = Properties()
            props.load(this::class.java.classLoader.getResourceAsStream("confluent.properties"))
            props.putAll(propOverrides)
            return props
        }

        val logger: Logger = LoggerFactory.getLogger(javaClass)
    }

    val kafkaProducer: KafkaProducer<Key, Value> = KafkaProducer(getProducerProperties(propOverrides))

    open fun send(topicName: String, key: Key, value: Value): Future<RecordMetadata?>? {
        val record = ProducerRecord(topicName, key, value)
        return kafkaProducer.send(record) { metadata: RecordMetadata?, exception: Exception? ->
            if (exception != null) {
                logger.error("Failed to send message: ${exception.message}")
                exception.printStackTrace(System.err)
            }
            else if (metadata != null) {
                logger.debug("Message sent successfully! Topic: ${metadata.topic()}, Partition: ${metadata.partition()}, Offset: ${metadata.offset()}")
            }
        }
    }

    fun close() {
        kafkaProducer.close()
    }
}
