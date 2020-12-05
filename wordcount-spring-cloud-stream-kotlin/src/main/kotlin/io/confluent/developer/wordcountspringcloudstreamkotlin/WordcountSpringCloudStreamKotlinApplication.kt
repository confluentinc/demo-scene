package io.confluent.developer.wordcountspringcloudstreamkotlin

import com.github.javafaker.Faker
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.*
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
  fun processWords(): Function<KStream<String?, String>, KStream<String, Long>>? {
    return Function { inputStream: KStream<String?, String> ->
      val stringSerde = Serdes.String()
      val stringStringKStream: KStream<String?, String> = inputStream
        .flatMapValues { value: String ->
          var list: Iterable<String> = value.toLowerCase().split("\\W+")
          list
        }
      stringStringKStream.print(Printed.toSysOut())

      val map = stringStringKStream
        .map { _: String?, value: String ->
          KeyValue(value, value)
        }
      map.print(Printed.toSysOut())

      val countsStream = map
        .groupByKey(Grouped.with(stringSerde, stringSerde))
        .count(Materialized.`as`("word-count-state-store"))
        .toStream()
      countsStream.to("counts", Produced.with(stringSerde, Serdes.Long()))
      countsStream
    }
  }
}


fun main(args: Array<String>) {
  runApplication<WordcountSpringCloudStreamKotlinApplication>(*args)


}

