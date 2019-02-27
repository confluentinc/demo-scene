package org.apache.kafka.streams.kotlin

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.KGroupedStream
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable

@DslMarker
annotation class KStreamsDsl

@KStreamsDsl
fun createTopology(block: StreamsBuilder.() -> Unit): Topology = StreamsBuilder().also(block).build()

@KStreamsDsl
fun <T, U> StreamsBuilder.kstream(topic: Collection<String>, block: KStream<T, U>.() -> Unit): KStream<T, U> =
        stream<T, U>(topic).also(block)

@KStreamsDsl
fun <K, V> KStream<K, V>.groupByKey(block: KGroupedStream<K, V>.() -> Unit): KGroupedStream<K, V> =
        groupByKey().also(block)

@KStreamsDsl
fun <K, V> KGroupedStream<K, V>.count(block: KTable<K, Long>.() -> Unit): KTable<K, Long> = count().also(block)