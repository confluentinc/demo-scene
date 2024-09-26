package io.confluent.devrel.spring.kfaker

import io.confluent.devrel.spring.model.Address
import io.confluent.devrel.spring.model.Customer
import io.confluent.devrel.spring.model.club.Checkin
import io.confluent.devrel.spring.model.club.Member
import io.confluent.devrel.spring.model.club.MembershipLevel
import io.github.serpro69.kfaker.faker
import java.time.Clock
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class BaseKFaker {

    companion object {
        val kFaker = faker {  }
        val techFaker = io.github.serpro69.kfaker.tech.faker {  }

        val dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    }

    fun address(): Address {
      val addressFaker = kFaker.address

      return Address.newBuilder()
          .setAddress1(addressFaker.streetAddress())
          .setAddress2(addressFaker.streetAddress())
          .setCity(addressFaker.city())
          .setState(addressFaker.state())
          .setPostalCode(addressFaker.postcode())
          .build()
    }

    fun customer(): Customer {

        val firstNameFaker = kFaker.name.firstName()
        val lastNameFaker = kFaker.name.lastName()

        val email = kFaker.internet.email("${firstNameFaker}.${lastNameFaker}")
        val age = (18..75).random().toLong()

        return Customer.newBuilder()
            .setId(kFaker.random.nextUUID())
            .setFirstName(firstNameFaker)
            .setLastName(lastNameFaker)
            .setEmail(email)
            .setMailingAddress(address())
            .setDob(dateTimeFormatter.format(kFaker.person.birthDate(age)))
            .build()
    }

    fun fakeMemberId(): String = kFaker.random.nextUUID().toString()

    fun member(memberId: String, level: MembershipLevel = MembershipLevel.STANDARD): Member {

        val firstNameFaker = kFaker.name.firstName()
        val lastNameFaker = kFaker.name.lastName()
        val email = kFaker.internet.email("${firstNameFaker}.${lastNameFaker}")

        val daysAgo = java.time.LocalDate.now().minusDays((100..10000).random().toLong())

        return Member.newBuilder()
            .setId(memberId)
            .setFirstName(firstNameFaker)
            .setLastName(lastNameFaker)
            .setEmail(email)
            .setMembershipLevel(level)
            .setJoinDate(daysAgo)
            .build()
    }

    fun checkin(memberId: String): Checkin {

        val checkinTime = Clock.system(ZoneId.systemDefault()).instant()
            .minus((1..15).random().toLong(), ChronoUnit.MINUTES)

        return Checkin.newBuilder()
            .setTxnId(kFaker.random.nextUUID())
            .setMemberId(memberId)
            .setTxnTimestamp(checkinTime)
            .build()
    }
}