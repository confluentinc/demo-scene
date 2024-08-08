package io.confluent.devrel.spring.kfaker

import io.confluent.devrel.spring.model.Address
import io.confluent.devrel.spring.model.Customer
import io.github.serpro69.kfaker.faker
import java.time.format.DateTimeFormatter

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
}