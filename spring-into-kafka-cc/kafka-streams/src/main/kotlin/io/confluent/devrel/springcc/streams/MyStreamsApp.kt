package io.confluent.devrel.springcc.streams

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyStreamsApp

fun main(args: Array<String>) {
    runApplication<MyStreamsApp>(*args)
}


