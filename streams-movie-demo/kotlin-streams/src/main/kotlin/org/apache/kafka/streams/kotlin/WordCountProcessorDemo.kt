package org.apache.kafka.streams.kotlin

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serdes.serdeFrom
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.PunctuationType
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.state.Stores.inMemoryKeyValueStore
import java.time.Duration
import java.util.*
import kotlin.Int
import kotlin.String

// from Java https://github.com/apache/kafka/blob/trunk/streams/examples/src/main/java/org/apache/kafka/streams/examples/wordcount/WordCountProcessorDemo.java 
fun main() {

  val inMemoryStore =
          Stores.keyValueStoreBuilder(inMemoryKeyValueStore("Counts"),
                  serdeFrom(String::class.java),
                  serdeFrom(Int::class.java))

  kafkaStreamsApp {
    config {
      // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
      StreamsConfig.APPLICATION_ID_CONFIG to "streams-wordcount-processor"
      StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092"
      StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG to 0
      StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String().javaClass
      StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String().javaClass
    }

    topology {
      // use https://zz85.github.io/kafka-streams-viz/ to visualize 
      describe = true

      source {
        name = "Source"
        topic = "streams-plaintext-input"
      }

      processor<String, String> {
        name = "Process"
        process = WordCountProcessor()
        parentNames = "Source"
      }

      stateStore<String, Int> {
        store = inMemoryStore
        processorName = "Process"
      }

      sink {
        name = "Sink"
        topic = "streams-wordcount-processor-output"
        parentNames = "Process"
      }
    }
  }
}

// maybe object?
class WordCountProcessor : Processor<String, String> {
  private var context: ProcessorContext? = null
  private var kvStore: KeyValueStore<String, Int>? = null

  override fun init(context: ProcessorContext) {
    this.context = context
    val ofSeconds = Duration.ofSeconds(1)

    this.context!!.schedule(ofSeconds, PunctuationType.STREAM_TIME) { timestamp ->
      kvStore!!.all().use { iter ->
        println("----------- $timestamp ----------- ")

        while (iter.hasNext()) {
          val entry = iter.next()

          println("[" + entry.key + ", " + entry.value + "]")

          context.forward(entry.key, entry.value.toString())
        }
      }
    }
    this.kvStore = context.getStateStore("Counts") as KeyValueStore<String, Int>
  }

  override fun process(dummy: String, line: String) {
    val words = line.toLowerCase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    for (word in words) {
      val oldValue = this.kvStore!!.get(word)

      if (oldValue == null) {
        this.kvStore!!.put(word, 1)
      } else {
        this.kvStore!!.put(word, oldValue + 1)
      }
    }

    context!!.commit()
  }

  override fun close() {}
}

