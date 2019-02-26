package org.apache.kafka.streams.kotlin

import org.apache.kafka.common.serialization.Serdes.serdeFrom
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped

inline fun <reified K, reified V> grouped() =
        Grouped.with(serdeFrom(K::class.java), serdeFrom(V::class.java))

inline fun <reified K, reified V> consumedWith() =
        Consumed.with(serdeFrom(K::class.java), serdeFrom(V::class.java))
