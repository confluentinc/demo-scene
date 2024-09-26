package io.confluent.devrel.springcc.streams

import io.confluent.devrel.springcc.streams.WordCountProcessor.Companion.INPUT_TOPIC
import io.confluent.devrel.springcc.streams.WordCountProcessor.Companion.OUTPUT_TOPIC
import kafka.tools.TestRecord
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.TopologyTestDriver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class WordCountProcessorUnitTest {

    @Test
    fun testCounts() {
        val streamsBuilder = StreamsBuilder()

        val processor = WordCountProcessor()
        processor.buildPipeline(streamsBuilder)

        val topology = streamsBuilder.build()
        TopologyTestDriver(topology).use { topologyTestDriver ->
            val inputTopic = topologyTestDriver.createInputTopic<String, String>(INPUT_TOPIC,
                StringSerializer(), StringSerializer())

            val outputTopic = topologyTestDriver.createOutputTopic(OUTPUT_TOPIC,
                StringDeserializer(), LongDeserializer())

            inputTopic.pipeInput("key1", "value1")
            inputTopic.pipeInput("key2", "value2")
            inputTopic.pipeInput("key3", "value1 and value2")

            assertThat(outputTopic.readKeyValuesToList())
                .containsAll(listOf(
                    KeyValue.pair("and", 1L),
                    KeyValue.pair("value1", 2L),
                    KeyValue.pair("value2", 2L)
                ))
        }
    }
}

