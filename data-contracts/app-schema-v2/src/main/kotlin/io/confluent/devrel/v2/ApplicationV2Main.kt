package io.confluent.devrel.v2

import io.confluent.devrel.v2.kafka.MembershipConsumer
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class ApplicationV2Main {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                println("Starting application main...")
                println(args.joinToString(" "))
                val consumer = MembershipConsumer()

                thread {
                    consumer.start(listOf("membership-avro"))
                }
            }
        }
    }
}