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
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

val schemaRegOutputDir = "${project.projectDir.absolutePath}/build/schema-registry-plugin"

tasks.registerSchemasTask {
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

    outputDirectory = schemaRegOutputDir
    pretty = true

    val baseBuildDir = "${project.projectDir}/src/main"
    val avroSchemaDir = "$baseBuildDir/avro"
    val rulesetDir = "$baseBuildDir/rulesets"
    val metadataDir = "$baseBuildDir/metadata"

    register {
        subject(inputSubject =  "membership-avro-value", type = "AVRO", file = "$avroSchemaDir/membership_v1.avsc")
            .setMetadata("$metadataDir/membership_major_version_1.json")
        subject(inputSubject =  "membership-avro-value", type = "AVRO", file = "$avroSchemaDir/membership_v2.avsc")
            .addLocalReference("validity-period", "$avroSchemaDir/validity_period.avsc")
            .setMetadata("$metadataDir/membership_major_version_2.json")
            .setRuleSet("$rulesetDir/membership_migration_rules.json")
    }
}
