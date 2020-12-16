package io.confluent.developer.kotlin.extensions;

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes.serdeFrom
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.Produced

object KSerdes {
  @DslMarker
  annotation class KStreamsDsl

  @KStreamsDsl
  inline fun <reified K, reified V> grouped(): Grouped<K, V> =
    Grouped.with(serdeFrom(K::class.java), serdeFrom(V::class.java))

  @KStreamsDsl
  inline fun <reified K, reified V> consumedWith(): Consumed<K, V> =
    Consumed.with(serdeFrom(K::class.java), serdeFrom(V::class.java))


  @KStreamsDsl
  inline fun <reified K, reified V> producedWith(): Produced<K, V> =
    Produced.with(serdeFrom(K::class.java), serdeFrom(V::class.java))


  @KStreamsDsl
  inline fun <reified K, V> producedWith(serde: Serde<V>): Produced<K, V> =
    Produced.with(serdeFrom(K::class.java), serde)
}

