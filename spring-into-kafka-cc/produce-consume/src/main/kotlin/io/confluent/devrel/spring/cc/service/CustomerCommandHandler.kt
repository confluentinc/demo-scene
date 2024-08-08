package io.confluent.devrel.spring.cc.service

import io.confluent.devrel.spring.model.Customer
import org.springframework.stereotype.Component

@Component
class CustomerCommandHandler {

    fun addCustomer(customer: Customer): Customer {
        return customer
    }

    fun updateCustomer(customer: Customer): Customer {
        return customer
    }

    fun deleteCustomer(customer: Customer): String {
        return customer.getId()
    }

}