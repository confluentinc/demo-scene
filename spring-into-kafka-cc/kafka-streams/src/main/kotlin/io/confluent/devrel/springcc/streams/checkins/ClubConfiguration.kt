package io.confluent.devrel.springcc.streams.checkins

import io.confluent.devrel.spring.model.club.Checkin
import io.confluent.devrel.spring.model.club.EnrichedCheckin
import io.confluent.devrel.spring.model.club.Member
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import kotlin.Any
import kotlin.String

@Configuration
@EnableKafkaStreams
class ClubConfiguration(
    @Value(value = "\${spring.kafka.bootstrap-servers}") val bootstrapServers: String,
    @Value(value = "\${spring.kafka.properties.[schema.registry.url]}") val schemaRegistryUrl: String,
    @Value(value = "\${spring.kafka.properties.[basic.auth.user.info]}") val schemaRegAuth: String,
    @Value("\${spring.kafka.properties.[sasl.jaas.config]}") val saslJaasConfig: String
) {

    @Bean(KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    fun streamsConfig(): KafkaStreamsConfiguration {
        return KafkaStreamsConfiguration(
            schemaRegistryProperties() + mapOf<String, Any>(
                StreamsConfig.APPLICATION_ID_CONFIG to "spring-cc-streams-app",
                StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String()::class.java,
                StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String()::class.java,
                StreamsConfig.SECURITY_PROTOCOL_CONFIG to "SASL_SSL",
                SaslConfigs.SASL_MECHANISM to "PLAIN",
                SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig,
                "schema.registry.basic.auth.user.info" to schemaRegAuth
            )
        )
    }

    private fun schemaRegistryProperties(): Map<String, Any> {
        return mapOf(
            AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE to "USER_INFO",
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
        )
    }

    private fun serdeProperties(): Map<String, Any> {
        return schemaRegistryProperties() + mapOf<String, Any>(
            "schema.registry.basic.auth.user.info" to schemaRegAuth,
            "specific.avro.reader" to true
        )
    }

    @Bean
    fun checkinSerde(): Serde<Checkin> {
        val serde = SpecificAvroSerde<Checkin>()
        serde.configure(serdeProperties(), false)
        return serde
    }

    @Bean
    fun memberSerde(): Serde<Member> {
        val serde = SpecificAvroSerde<Member>()
        serde.configure(serdeProperties(), false)
        return serde
    }

    @Bean
    fun enrichedCheckinSerde(): Serde<EnrichedCheckin> {
        val serde = SpecificAvroSerde<EnrichedCheckin>()
        serde.configure(serdeProperties(), false)
        return serde
    }

}