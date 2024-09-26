package io.confluent.devrel.springcc.streams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.Stores
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class WordCountProcessor {

    val logger = LoggerFactory.getLogger(WordCountProcessor::class.java)

    companion object {
        val INPUT_TOPIC = "wc-input-topic"
        val OUTPUT_TOPIC = "wc-output-topic"
        val COUNTS_STORE = "wc-counts"
    }

    val STRING_SERDE = Serdes.String()
    val LONG_SERDE = Serdes.Long()

    @Autowired
    fun buildPipeline(streamsBuilder: StreamsBuilder) {

        val messageStream = streamsBuilder
            .stream(INPUT_TOPIC, Consumed.with(STRING_SERDE, STRING_SERDE))
            .peek {_, value -> logger.debug("*** raw value {}", value)}

        val wordCounts = messageStream
            .mapValues { v -> v.lowercase() }
            .peek {_, value -> logger.info("*** lowercase value = {}", value)}
            .flatMapValues { v -> v.split("\\W+".toRegex()) }
            .groupBy({ _, word -> word }, Grouped.with(Serdes.String(), Serdes.String()))
            .count(Materialized.`as`<String, Long>(Stores.persistentKeyValueStore(COUNTS_STORE))
                .withKeySerde(STRING_SERDE)
                .withValueSerde(LONG_SERDE))

        wordCounts.toStream().to(OUTPUT_TOPIC, Produced.with(STRING_SERDE, LONG_SERDE))
    }
}