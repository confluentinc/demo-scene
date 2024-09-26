package io.confluent.devrel.springcc.streams.checkins

import io.confluent.devrel.spring.model.club.Checkin
import io.confluent.devrel.spring.model.club.EnrichedCheckin
import io.confluent.devrel.spring.model.club.Member
import io.confluent.devrel.spring.model.club.MembershipLevel.GOLD
import io.confluent.devrel.spring.model.club.MembershipLevel.PLATINUM
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Joined
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MemberCheckinProcessor(
    val checkinSerde: Serde<Checkin>,
    val memberSerde: Serde<Member>,
    val enrichedCheckinSerde: Serde<EnrichedCheckin>
) {

    val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        val CHECKIN_TOPIC = "checkin-avro"
        val MEMBER_TOPIC = "membership-avro"
        val ENRICHED_CHECKIN_TOPIC = "enriched-checkin-avro"
    }

    /**
     * Join stream of Checkin objects with table of Member, based on the memberId.
     */
    @Autowired
    fun buildPipeline(streamsBuilder: StreamsBuilder) {

        val checkins = streamsBuilder.stream(CHECKIN_TOPIC, Consumed.with(Serdes.String(), checkinSerde))
            .peek { _, checkin -> logger.debug("checkin -> {}", checkin) }

        val members = streamsBuilder.table(MEMBER_TOPIC, Consumed.with(Serdes.String(), memberSerde))
            .filter { _, m -> listOf(PLATINUM, GOLD).contains(m.membershipLevel) }

        val joined = checkins.join(members, { checkin, member ->
            logger.debug("matched member {} to checkin {}", member, checkin.txnId)
            EnrichedCheckin.newBuilder()
                .setMemberId(member.id)
                .setCheckinTxnId(checkin.txnId)
                .setTxnTimestamp(checkin.txnTimestamp)
                .setMembershipLevel(member.membershipLevel)
                .build()
        }, Joined.with(Serdes.String(), checkinSerde, memberSerde))

        joined.to(ENRICHED_CHECKIN_TOPIC, Produced.with(Serdes.String(), enrichedCheckinSerde))
    }
}