package io.confluent.devrel.spring.cc.kafka

import io.confluent.devrel.spring.cc.kafka.CustomerCommandKafka.Companion.topicName
import io.confluent.devrel.spring.command.CustomerAction
import io.confluent.devrel.spring.command.CustomerCommand
import io.confluent.devrel.spring.kfaker.BaseKFaker
import io.confluent.devrel.spring.model.Customer
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration

@Component
class CustomerCommandProducer(@Autowired private val kafkaTemplate: KafkaTemplate<String, CustomerCommand>) {

    private val LOG = LoggerFactory.getLogger(javaClass)

    companion object {
        val baseKFaker = BaseKFaker()
    }

    suspend fun startSend(howLong: Duration, interval: Duration) {
        val until = Clock.System.now().plus(howLong)

        LOG.info("Sending events to $topicName every $interval, until $until")

        while(Clock.System.now().compareTo(until) < 0) {
            sendCustomerCommand(baseKFaker.customer())
            delay(interval.inWholeMilliseconds)
        }
    }

    fun sendCustomerCommand(customer: Customer, customerAction: CustomerAction = CustomerAction.ADD) {
        val command = CustomerCommand.newBuilder()
            .setCustomer(customer)
            .setAction(customerAction)
            .build()

        val record = ProducerRecord(topicName, customer.getEmail(), command)
        val future: CompletableFuture<SendResult<String, CustomerCommand>> = kafkaTemplate.send(record)

        future.whenComplete { result, exception ->
            if (exception != null) {
                LOG.error("ERROR sending to Kafka", exception)
            } else {
                val metadata = result.recordMetadata
                LOG.info("Message sent to topic ${metadata.topic()}, partition: ${metadata.partition()}, offset: ${metadata.offset()}")
            }
        }
    }
}