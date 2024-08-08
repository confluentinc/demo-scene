package io.confluent.devrel.spring.cc.kafka

import io.confluent.devrel.spring.cc.kafka.CustomerCommandKafka.Companion.topicName
import io.confluent.devrel.spring.cc.service.CustomerCommandHandler
import io.confluent.devrel.spring.command.CustomerAction
import io.confluent.devrel.spring.command.CustomerCommand
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CustomerCommandConsumer(@Autowired private val handler: CustomerCommandHandler) {

    private val LOG = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [topicName])
    fun processCustomerCommand(command: CustomerCommand) {
        runBlocking {
            LOG.info("processing CustomerCommand -> $command")

            when(command.getAction()) {
                CustomerAction.ADD -> handler.addCustomer(command.getCustomer())
                CustomerAction.UPDATE -> handler.updateCustomer(command.getCustomer())
                CustomerAction.DELETE -> handler.deleteCustomer(command.getCustomer())
                CustomerAction.UNKNOWN -> throw IllegalArgumentException("unknown command")
            }
        }
    }
}