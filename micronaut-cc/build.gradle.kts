plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.2"
    id("io.micronaut.aot") version "4.4.2"
    id("com.bakdata.avro") version "1.0.0"
}

version = "0.1"
group = "example.micronaut"

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven")
}

val confluentVersion = project.properties.get("confluentVersion")

dependencies {
    annotationProcessor("io.micronaut.data:micronaut-data-processor")
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")

    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    implementation("io.micronaut.kafka:micronaut-kafka")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")

    implementation("org.apache.avro:avro:1.12.0")
    implementation("io.confluent:kafka-avro-serializer:${confluentVersion}")
    implementation("io.confluent:kafka-schema-rules:${confluentVersion}")

    implementation("org.projectlombok:lombok:1.18.36")

    implementation("net.datafaker:datafaker:2.4.1")

    compileOnly("io.micronaut:micronaut-http-client")

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.yaml:snakeyaml")

    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("io.micronaut.test:micronaut-test-rest-assured")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.9.5")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("org.testcontainers:junit-jupiter:1.20.3")
    testImplementation("org.testcontainers:kafka:1.20.3")
    testImplementation("org.testcontainers:mysql:1.20.3")
    testImplementation("org.testcontainers:testcontainers:1.20.3")
}


application {
    mainClass = "io.confluent.devrel.Application"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

sourceSets {
    main {
        java.srcDirs("src/main/java", "build/generated-main-avro-java")
    }
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("example.micronaut.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}



