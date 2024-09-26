package io.confluent.devrel.springcc.streams.checkins

import io.confluent.devrel.spring.kfaker.BaseKFaker
import io.confluent.devrel.spring.model.club.Checkin
import io.confluent.devrel.spring.model.club.EnrichedCheckin
import io.confluent.devrel.spring.model.club.Member
import io.confluent.devrel.spring.model.club.MembershipLevel
import io.confluent.devrel.springcc.streams.checkins.MemberCheckinProcessor.Companion.CHECKIN_TOPIC
import io.confluent.devrel.springcc.streams.checkins.MemberCheckinProcessor.Companion.ENRICHED_CHECKIN_TOPIC
import io.confluent.devrel.springcc.streams.checkins.MemberCheckinProcessor.Companion.MEMBER_TOPIC
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.Properties
import kotlin.test.assertEquals

class MemberCheckinProcessorUnitTest {

    private val schemaRegistryUrl = "mock://schema-registry"
    private val schemaRegistryClient = MockSchemaRegistryClient()

    lateinit var checkinSerde: Serde<Checkin>
    lateinit var memberSerde: Serde<Member>
    lateinit var enrichedCheckinSerde: Serde<EnrichedCheckin>

    lateinit var testDriver: TopologyTestDriver

    lateinit var checkinProcessor: MemberCheckinProcessor

    lateinit var checkinTopic: TestInputTopic<String, Checkin>
    lateinit var memberTopic: TestInputTopic<String, Member>
    lateinit var enrichedOutputTopic: TestOutputTopic<String, EnrichedCheckin>

    companion object {
        val baseKFaker = BaseKFaker()
    }

    @BeforeEach
    fun setup() {
        val streamProps = Properties().apply {
            {
                put(StreamsConfig.APPLICATION_ID_CONFIG, "test")
                put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
                put(StreamsConfig.CLIENT_ID_CONFIG, "client-id")
                put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
            }
        }

        val testSerdeConfig = mapOf(
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl
        )

        checkinSerde = SpecificAvroSerde<Checkin>()
        checkinSerde.configure(testSerdeConfig, false)

        memberSerde = SpecificAvroSerde<Member>()
        memberSerde.configure(testSerdeConfig, false)

        enrichedCheckinSerde = SpecificAvroSerde<EnrichedCheckin>()
        enrichedCheckinSerde.configure(testSerdeConfig, false)

        val builder = StreamsBuilder()
        checkinProcessor = MemberCheckinProcessor(checkinSerde, memberSerde, enrichedCheckinSerde)
        checkinProcessor.buildPipeline(builder)

        testDriver = TopologyTestDriver(builder.build(), streamProps)

        checkinTopic = testDriver.createInputTopic(
            CHECKIN_TOPIC,
            Serdes.String().serializer(),
            checkinSerde.serializer()
        )
        memberTopic = testDriver.createInputTopic(
            MEMBER_TOPIC,
            Serdes.String().serializer(),
            memberSerde.serializer()
        )

        enrichedOutputTopic = testDriver.createOutputTopic(
            ENRICHED_CHECKIN_TOPIC,
            Serdes.String().deserializer(),
            enrichedCheckinSerde.deserializer()
        )
    }

    @AfterEach
    fun cleanup() {
        testDriver.close()
    }

    @ParameterizedTest(name = "MembershipLevel: {0}")
    @EnumSource(names = ["PLATINUM", "GOLD"])
    fun `test gets a match on a PLATINUM or GOLD member`(level: MembershipLevel) {
        val memberId = baseKFaker.fakeMemberId()
        val member = baseKFaker.member(memberId, level)
        val checkin = baseKFaker.checkin(memberId)

        memberTopic.pipeInput(memberId, member)
        checkinTopic.pipeInput(memberId, checkin)

        val outputValues = enrichedOutputTopic.readValuesToList()
        assertEquals(1, outputValues.count())
        assertThat(outputValues).containsExactly(
            EnrichedCheckin.newBuilder()
                .setMemberId(memberId)
                .setCheckinTxnId(checkin.txnId)
                .setTxnTimestamp(checkin.txnTimestamp)
                .setMembershipLevel(member.membershipLevel)
                .build()
        )
    }

    @ParameterizedTest(name = "MembershipLevel: {0}")
    @EnumSource(names = ["PLATINUM", "GOLD"])
    fun `test gets NO match on a PLATINUM or GOLD member`(level: MembershipLevel) {
        val memberId = baseKFaker.fakeMemberId()
        val member = baseKFaker.member(memberId, level)
        // reversing the memberId value to force a mismatch, no results in the leftJoin.
        val checkin = baseKFaker.checkin(memberId.reversed())

        memberTopic.pipeInput(memberId, member)
        checkinTopic.pipeInput(checkin.memberId, checkin)

        val outputValues = enrichedOutputTopic.readValuesToList()
        assertThat(outputValues).isEmpty()
    }

    @ParameterizedTest(name = "MembershipLevel: {0}")
    @EnumSource(names = ["STANDARD", "SILVER"])
    fun `test gets NO match on a STANDARD or SILVER member`(level: MembershipLevel) {
        val memberId = baseKFaker.fakeMemberId()
        val member = baseKFaker.member(memberId, level)
        val checkin = baseKFaker.checkin(memberId)

        memberTopic.pipeInput(memberId, member)
        checkinTopic.pipeInput(checkin.memberId, checkin)

        val outputValues = enrichedOutputTopic.readValuesToList()
        assertThat(outputValues).isEmpty()
    }
}