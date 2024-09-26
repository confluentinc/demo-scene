package io.confluent.devrel.springcc.streams

import io.confluent.devrel.springcc.streams.WordCountProcessor.Companion.COUNTS_STORE
import io.confluent.devrel.springcc.streams.WordCountProcessor.Companion.INPUT_TOPIC
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WordCountRestController(val kafkaTemplate: KafkaTemplate<String, String>,
    val factoryBean: StreamsBuilderFactoryBean) {

    private val log = LoggerFactory.getLogger(javaClass)

    @PostMapping("/message")
    fun postMessage(@RequestBody message: String) {
        log.debug(message)
        kafkaTemplate.send(INPUT_TOPIC, message, message)
    }

    @GetMapping("/count/{word}")
    fun getWordCount(@PathVariable("word") word: String): Long {
        val streams = factoryBean.kafkaStreams

        val countResult = streams?.store<ReadOnlyKeyValueStore<String, Long>>(StoreQueryParameters.fromNameAndType(
            COUNTS_STORE, QueryableStoreTypes.keyValueStore()))?.get(word)
        log.info("result for word {} = {}" , word, countResult)

        return countResult ?: 0L
    }
}