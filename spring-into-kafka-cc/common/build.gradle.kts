plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("com.bakdata.avro")
    kotlin("plugin.serialization")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven")
}

sourceSets {
    main {
        kotlin.srcDirs("src/main/kotlin", "build/generated-main-avro-java")
    }
}

val fakerVersion = "2.0.0-rc.3"

dependencies {
//    implementation("org.slf4j:slf4j-api:2.0.11")
//    implementation("org.slf4j:slf4j-simple:2.0.11")
//    implementation("ch.qos.logback:logback-core:1.4.14")

    implementation(platform("io.github.serpro69:kotlin-faker-bom:$fakerVersion"))
    implementation("io.github.serpro69:kotlin-faker")
    implementation("io.github.serpro69:kotlin-faker-books")
    implementation("io.github.serpro69:kotlin-faker-tech")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")

    implementation("org.apache.kafka:kafka-clients:3.7.0")
    implementation("io.confluent:kafka-avro-serializer:7.6.0")
    implementation("io.confluent:kafka-schema-rules:7.6.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
