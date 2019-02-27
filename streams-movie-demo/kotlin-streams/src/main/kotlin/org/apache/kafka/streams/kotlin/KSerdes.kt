package org.apache.kafka.streams.kotlin

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes.serdeFrom
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Produced

object KSerdes {

  @KStreamsDsl
  inline fun <reified K, reified V> grouped(): Grouped<K, V> =
          Grouped.with(serdeFrom(K::class.java), serdeFrom(V::class.java))

  @KStreamsDsl
  inline fun <reified K, reified V> consumedWith(): Consumed<K, V> =
          Consumed.with(serdeFrom(K::class.java), serdeFrom(V::class.java))


  @KStreamsDsl
  inline fun <reified K, reified V> producedWith(): Produced<K, V> = Produced.with(serdeFrom(K::class.java), serdeFrom(V::class.java))


  @KStreamsDsl
  inline fun <reified K, V> producedWith(serde: Serde<V>): Produced<K, V> = Produced.with(serdeFrom(K::class.java), serde)

  @KStreamsDsl
  inline fun <reified T> serdeFrom(config: Map<String, String> = HashMap(), keySerde: Boolean = false): SpecificAvroSerde<T> where T : SpecificRecordBase {
    val specificAvroSerde = SpecificAvroSerde<T>()
    specificAvroSerde.configure(config, keySerde)
    return specificAvroSerde
  }
}

