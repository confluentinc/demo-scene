package io.confluent.devrel.spring.cc

import io.confluent.devrel.spring.cc.kafka.CustomerCommandProducer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Component
class ProduceConsumeRunner(private val customerCommandProducer: CustomerCommandProducer) : CommandLineRunner, CoroutineScope {

    private val LOG = LoggerFactory.getLogger(ProduceConsumeApp::class.java)

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    @Throws(Exception::class)
    override fun run(args: Array<String>) {
        val parser = ArgParser("producer-consume-app")

        val delayArg by parser.option(
            ArgType.Int,
            shortName = "i", fullName = "delay",
            description = "message send delay, milliseconds")
            .default(200)
        val durationArg by parser.option(
            ArgType.Int,
            shortName = "d", fullName = "duration",
            description = "send duration, seconds")
            .default(20)
        parser.parse(args)

        val delay = delayArg.toDuration(DurationUnit.MILLISECONDS)
        val duration = durationArg.toDuration(DurationUnit.SECONDS)

        launch {
            LOG.info("Producing a CustomerCommand event every $delay for the next $duration.")
            customerCommandProducer.startSend(duration, delay)
        }
    }
}