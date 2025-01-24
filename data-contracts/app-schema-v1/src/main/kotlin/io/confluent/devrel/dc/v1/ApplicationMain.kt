package io.confluent.devrel.dc.v1

import io.confluent.devrel.Membership
import io.confluent.devrel.dc.v1.kafka.MembershipConsumer
import io.confluent.devrel.dc.v1.kafka.MembershipProducer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import java.time.LocalDate
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ApplicationMain {

    companion object {


        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                println("Starting application main...")
                println(args.joinToString(" "))
                val parser = ArgParser("schema-v1")

                val interval by parser.option(ArgType.Int,
                    shortName = "i", fullName = "interval",
                    description = "message send interval, seconds")
                    .default(1)
                val duration by parser.option(ArgType.Int,
                    shortName = "d", fullName = "duration",
                    description = "how long to run, seconds")
                    .default(100)
                parser.parse(args)

                val messageInterval = interval.toDuration(DurationUnit.SECONDS)
                val sendDuration = duration.toDuration(DurationUnit.SECONDS)

                val producer = MembershipProducer()
                val consumer = MembershipConsumer()

                thread {
                    consumer.start(listOf("membership-avro"))
                }

                coroutineScope {
                    launch {
                        val until = Clock.System.now().plus(sendDuration)
                        while(Clock.System.now().compareTo(until) < 0) {
                            val userId = UUID.randomUUID().toString()
                            val membership = Membership.newBuilder()
                                .setUserId(userId)
                                .setStartDate(LocalDate.now().minusDays(Random.nextLong(100, 1000)))
                                .setEndDate(LocalDate.now().plusWeeks(Random.nextLong(1, 52)))
                                .build()
                            producer.send("membership-avro", userId, membership)
                            delay(messageInterval.inWholeSeconds)
                        }
                    }
                }
                producer.close()
            }
        }
    }
}