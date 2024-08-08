plugins {
	id("org.springframework.boot") version "3.3.1" apply false
	id("io.spring.dependency-management") version "1.1.5" apply false
	id("org.graalvm.buildtools.native") version "0.10.2" apply false
	kotlin("jvm") version "1.9.24" apply false
	kotlin("plugin.spring") version "1.9.24" apply false
	id("com.bakdata.avro") version "1.0.0" apply false
	kotlin("plugin.serialization") version "2.0.0" apply false
}

group = "io.confluent.devrel"
version = "0.0.1-SNAPSHOT"