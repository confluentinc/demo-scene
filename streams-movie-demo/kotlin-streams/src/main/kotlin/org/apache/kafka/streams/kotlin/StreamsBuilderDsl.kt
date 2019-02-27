package org.apache.kafka.streams.kotlin

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable

@DslMarker
annotation class KStreamsDsl

@KStreamsDsl
fun createTopology(block: StreamsBuilder.() -> Unit): Topology {
  val builder = StreamsBuilder()
  block(builder)
  return builder.build()
}

@KStreamsDsl
fun <T, U> StreamsBuilder.kstream(topic: Collection<String>, block: KStream<T, U>.() -> Unit): KStream<T, U> {
  val stream: KStream<T, U> = this.stream<T, U>(topic)
  block(stream)
  return stream
}

@KStreamsDsl
fun <K, V> KStream<K, V>.groupByKey(block: KGroupedStream<K, V>.() -> Unit): KGroupedStream<K, V> {
  val kGroupedStream = this.groupByKey()
  block(kGroupedStream)
  return kGroupedStream
}

@KStreamsDsl
fun <K, V> KGroupedStream<K, V>.count(block: KTable<K, Long>.() -> Unit): KTable<K, Long> {
  val kTable = this.count()
  block(kTable)
  return kTable
}