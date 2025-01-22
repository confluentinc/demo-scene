package io.confluent.devrel.datacontracts.shared

import java.io.File
import java.io.InputStream
import java.util.Properties

object ConfigLoader {

    fun loadPropsFromFile(path: String): Properties {
        val properties = Properties()
        val stream: InputStream? = File(path).inputStream()
        stream.use {
            println("reading properties from $path")
            properties.load(stream)
        }

        return properties
            .mapKeys { it.key.toString() }
            .mapValues { it.value.toString().replace("\"", "") }
            .toProperties()
    }

}