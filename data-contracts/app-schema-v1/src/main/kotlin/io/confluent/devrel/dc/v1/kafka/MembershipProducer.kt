package io.confluent.devrel.dc.v1.kafka

import io.confluent.devrel.Membership
import io.confluent.devrel.datacontracts.shared.BaseProducer
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import org.apache.kafka.clients.producer.ProducerConfig

class MembershipProducer: BaseProducer<String, Membership>(mapOf(
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "io.confluent.kafka.serializers.KafkaAvroSerializer",
    ProducerConfig.CLIENT_ID_CONFIG to "membership-producer-app-v1",
    AbstractKafkaSchemaSerDeConfig.LATEST_COMPATIBILITY_STRICT to true,
    AbstractKafkaSchemaSerDeConfig.USE_LATEST_VERSION to false,
    AbstractKafkaSchemaSerDeConfig.USE_LATEST_WITH_METADATA to "major_version=1"
))