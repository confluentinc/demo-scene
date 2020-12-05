package io.confluent.developer.wordcount

import com.github.javafaker.Faker
import io.confluent.developer.kotlin.extensions.KSerdes.grouped
import io.confluent.developer.kotlin.extensions.KSerdes.producedWith
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier


@SpringBootApplication
class WordcountSpringCloudStreamKotlinApplication {

  @Bean
  fun produceChuckNorris(): Supplier<Message<String>> {
    return Supplier {
      MessageBuilder.withPayload(Faker.instance().chuckNorris().fact()).build()
    }
  }

  @Bean
  fun consumeChuckNorris(): Consumer<Message<String>> {
    return Consumer { s: Message<String> ->
      println(
        "FACT: \u001B[3m «" + s.payload + "\u001B[0m»"
      )
    }
  }

  @Bean
  fun processWords(): Function<KStream<String?, String>, KStream<String, Long>> {
    return Function { inputStream: KStream<String?, String> ->
      val stringSerde = Serdes.String()
      val countsStream = inputStream
        .flatMapValues { value: String -> value.toLowerCase().split("\\W+".toRegex()) }
        .map { _: String?, value: String -> KeyValue(value, value) }
        .groupByKey(grouped<String, String>())
        .count(Materialized.`as`("word-count-state-store"))
        .toStream()
      countsStream.to("counts", producedWith<String,Long>())
      countsStream
    }
  }
}

fun main(args: Array<String>) {
  runApplication<WordcountSpringCloudStreamKotlinApplication>(*args)
}
