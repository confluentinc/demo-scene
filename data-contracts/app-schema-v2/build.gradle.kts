import java.io.FileInputStream
import java.util.Properties

buildscript {
    repositories {
        mavenCentral()
        maven("https://packages.confluent.io/maven/")
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}

plugins {
    kotlin("jvm") version "2.0.21"
    id("com.google.protobuf") version "0.9.4"
    id("com.github.imflog.kafka-schema-registry-gradle-plugin") version "2.1.0"
    id("com.bakdata.avro") version "1.2.1"
}

group = "io.confluent.devrel"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin", "build/generated-main-avro-java")
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("io.confluent.devrel:data-contracts-shared:+")

    implementation("org.apache.kafka:kafka-clients:${project.property("kafkaVersion")}")
    implementation("io.confluent:kafka-avro-serializer:${project.property("confluentVersion")}")

    implementation("io.github.serpro69:kotlin-faker:${project.property("fakerVersion")}")
    implementation("io.github.serpro69:kotlin-faker-books:${project.property("fakerVersion")}")
    implementation("io.github.serpro69:kotlin-faker-tech:${project.property("fakerVersion")}")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
}

kotlin {
    jvmToolchain(17)
}

val schemaRegOutputDir = "${project.projectDir.absolutePath}/build/schema-registry-plugin"

tasks.downloadSchemasTask {
    doFirst {
        mkdir(schemaRegOutputDir)
    }
}

schemaRegistry {
    val srProperties = Properties()
    // At the moment, this is a file with which we are LOCALLY aware.
    // In an ACTUAL CI/CD workflow, this would be externalized, perhaps provided from a base build image or other parameter.
    srProperties.load(FileInputStream(File("${project.projectDir.absolutePath}/../shared/src/main/resources/confluent.properties")))

    url = srProperties.getProperty("schema.registry.url")

    val srCredTokens = srProperties.get("basic.auth.user.info").toString().split(":")
    credentials {
        username = srCredTokens[0]
        password = srCredTokens[1]
    }
    outputDirectory = "${System.getProperty("user.home")}/tmp/schema-registry-plugin"
    pretty = true

    download {
        // download the membership avro schema, version 2
        subject("membership-avro-value", "${projectDir}/src/main/avro", 2)
    }
}

tasks.clean {
    doFirst {
        delete(fileTree("${projectDir}/src/main/avro/").include("**/*.avsc"))
    }
}

tasks.register("generateCode") {
    group = "source generation"
    description = "wrapper task for all source generation"
    dependsOn("downloadSchemasTask", "generateAvroJava", "generateProto")
}
