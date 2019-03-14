package org.apache.kafka.streams.kotlin

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Produced
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * Demonstrates, using the high-level KStream DSL, how to implement the WordCount program
 * that computes a simple word occurrence histogram from an input text.
 *
 *
 * In this example, the input stream reads from a topic named "streams-plaintext-input", where the values of messages
 * represent lines of text; and the histogram output is written to topic "streams-wordcount-output" where each record
 * is an updated count of a single word.
 *
 *
 * Before running this example you must create the input topic and the output topic (e.g. via
 * `bin/kafka-topics.sh --create ...`), and write some data to the input topic (e.g. via
 * `bin/kafka-console-producer.sh`). Otherwise you won't see any data arriving in the output topic.
 */
object WordCountDemo {

  @JvmStatic
  fun main(args: Array<String>) {
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "streams-wordcount"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    props[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name

    // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
    // Note: To re-run the demo, you need to use the offset reset tool:
    // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
    props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

    val builder = StreamsBuilder()

    val source = builder.stream<String, String>("streams-plaintext-input")

    val counts = source
            .flatMapValues { value -> Arrays.asList(*value.toLowerCase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) }
            .groupBy { key, value -> value }
            .count()

    // need to override value serde to Long type
    counts.toStream().to("streams-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()))

    val streams = KafkaStreams(builder.build(), props)
    val latch = CountDownLatch(1)

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(object : Thread("streams-wordcount-shutdown-hook") {
      override fun run() {
        streams.close()
        latch.countDown()
      }
    })

    try {
      streams.start()
      latch.await()
    } catch (e: Throwable) {
      System.exit(1)
    }

    System.exit(0)
  }
}