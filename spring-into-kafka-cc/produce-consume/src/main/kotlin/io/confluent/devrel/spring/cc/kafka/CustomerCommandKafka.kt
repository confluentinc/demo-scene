package io.confluent.devrel.spring.cc.kafka

interface CustomerCommandKafka {
    companion object {
        const val topicName = "customer-commands-avro"
    }
}