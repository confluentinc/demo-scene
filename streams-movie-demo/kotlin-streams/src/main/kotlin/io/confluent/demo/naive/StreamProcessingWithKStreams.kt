package io.confluent.demo.naive

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Produced
import java.util.*

object StreamProcessingUsingKafkaStreams {

  @JvmStatic
  fun main(args: Array<String>) {
    val streamsBuilder = StreamsBuilder()
    val stream = streamsBuilder.stream<String, Long>(Arrays.asList("A", "B"))

    // actual work
    stream.groupByKey()
            .count()
            .toStream()
            .to("group-by-counts",
                    Produced.with(Serdes.String(), Serdes.Long()))

    //----

    val topology = streamsBuilder.build()
    val kafkaStreams = KafkaStreams(topology, streamsProperties())
    kafkaStreams.start()
  }

  private fun streamsProperties(): Properties {
    return Properties()
  }
}