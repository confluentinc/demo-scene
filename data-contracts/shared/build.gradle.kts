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
    `maven-publish`
}

group = "io.confluent.devrel"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.slf4j:slf4j-api:${project.property("slf4jVersion")}")
    implementation("org.slf4j:slf4j-simple:${project.property("slf4jVersion")}")
    implementation("ch.qos.logback:logback-core:1.4.14")

    implementation("io.github.serpro69:kotlin-faker:${project.property("fakerVersion")}")
    implementation("io.github.serpro69:kotlin-faker-books:${project.property("fakerVersion")}")
    implementation("io.github.serpro69:kotlin-faker-tech:${project.property("fakerVersion")}")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    implementation("io.grpc:grpc-stub:${project.property("grpcVersion")}")
    implementation("io.grpc:grpc-protobuf:${project.property("grpcVersion")}")
    implementation("com.google.protobuf:protobuf-java:${project.property("protobufVersion")}")

    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("io.confluent:kafka-avro-serializer:${project.property("confluentVersion")}")
    implementation("io.confluent:kafka-protobuf-serializer:${project.property("confluentVersion")}")
    implementation("io.confluent:kafka-schema-rules:${project.property("confluentVersion")}")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.confluent.devrel"
            artifactId = "data-contracts-shared"
            version = "0.0.1"

            from(components["kotlin"])
        }
    }
}
