package org.apache.kafka.streams.kotlin

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorSupplier
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.StoreBuilder
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@DslMarker
annotation class KStreamsDsl

// PAPI DSL --------------------------------------------------

/**
 * Kafka Streams app entry point.
 * Consists of Topology and Config.
 */
@KStreamsDsl
fun kafkaStreamsApp(block: KafkaStreamsModel.() -> Unit) {
  val app = KafkaStreamsModel().apply(block)
  if (app.describe) println(app.dag?.describe().toString())
  val kafkaStreams = KafkaStreams(app.dag, app.config)

  val latch = CountDownLatch(1)

  // opinionated ;)
  Runtime.getRuntime().addShutdownHook(thread(start = false) {
    kafkaStreams.close()
    latch.countDown()
  })
  kafkaStreams.start()
  println("""
    KafkaStreams app "${app.config?.get(StreamsConfig.APPLICATION_ID_CONFIG)}" is running...
""")
  latch.await()
}

/**
 * @property describe property allows to control if topology visualization is needed
 */
@KStreamsDsl
class KafkaStreamsModel {
  var dag: Topology? = null
  var describe: Boolean = false
  var config: Properties? = null
}

@KStreamsDsl
fun KafkaStreamsModel.topology(block: Topology.() -> Unit) {
  val topology = Topology()
  block(topology)
  this.dag = topology
}

interface /*enum*/ Key<T>
interface KStreamsConfig {
  infix fun <T> String.to(t: T)
}

@KStreamsDsl
fun KafkaStreamsModel.config(a: KStreamsConfig.() -> Unit) {
  val p = Properties()
  object : KStreamsConfig {
    override fun <T> String.to(t: T) {
      p.put(this, t)
    }
  }.a()
  this.config = p
}

@KStreamsDsl
fun Topology.source(block: Source.() -> Unit): Topology {
  val source = Source().apply(block)
  return this.addSource(source.name, source.topic)
}

class Source {
  var name: String = ""
  var topic: String = ""
}


@KStreamsDsl
fun Topology.sink(block: Sink.() -> Unit): Topology {
  val sink = Sink().apply(block)
  return this.addSink(sink.name, sink.topic, sink.parentNames)
}

/**
 * simple pojo model/holder for sink
 */
@KStreamsDsl
class Sink {
  var name: String = ""
  var topic: String = ""
  var parentNames: String = ""
}

@KStreamsDsl
fun <K, V> Topology.processor(block: KProcessor<K, V>.() -> Unit): KProcessor<K, V> {
  val pm = KProcessor<K, V>().apply(block)
  //this.addProcessor(pm.name, ProcessorSupplier<K, V> { pm.process })
  this.addProcessor(pm.name, ProcessorSupplier<K, V> { pm.process }, pm.parentNames)
  return pm
}

/**
 * simple pojo model/holder for Processor.
 * Uses K-prefix to avoid name clash
 *
 * @see Processor
 */
@KStreamsDsl
class KProcessor<K, V> {
  var name: String = ""
  var process: Processor<K, V>? = null
  // list maybe?
  var parentNames: String = ""
}

@KStreamsDsl
fun <K, V> Topology.stateStore(block: KStateStore<K, V>.() -> Unit) {
  val store = KStateStore<K, V>().apply(block)
  this.addStateStore(store.store, store.processorName)
}

@KStreamsDsl
class KStateStore<K, V> {
  var processorName: String? = null
  var store: StoreBuilder<KeyValueStore<K, V>>? = null
}


// Fluent KStreams API DSL Scratch pad
/*
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
*/
