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

val fakerVersion = providers.gradleProperty("kotlin_faker_version").get()
val kafkaVersion = providers.gradleProperty("kafka_version").get()
val confluentVersion = providers.gradleProperty("confluent_version").get()

dependencies {
    implementation(platform("io.github.serpro69:kotlin-faker-bom:$fakerVersion"))
    implementation("io.github.serpro69:kotlin-faker")
    implementation("io.github.serpro69:kotlin-faker-books")
    implementation("io.github.serpro69:kotlin-faker-tech")

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")

    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    implementation("io.confluent:kafka-avro-serializer:$confluentVersion")

    implementation("io.confluent:kafka-schema-rules:$confluentVersion")

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

