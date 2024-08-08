package io.confluent.devrel.spring.cc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProduceConsumeApp {

}

fun main(args: Array<String>) {
    runApplication<ProduceConsumeApp>(*args)
}