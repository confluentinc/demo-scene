package io.confluent.devrel.spring.cc.kafka

import io.confluent.devrel.spring.cc.service.CustomerCommandHandler
import io.confluent.devrel.spring.command.CustomerAction
import io.confluent.devrel.spring.command.CustomerCommand
import io.confluent.devrel.spring.model.Address
import io.confluent.devrel.spring.model.Customer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CustomerCommandConsumerTest {

    private val handler = mockk<CustomerCommandHandler>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private val customer = Customer.newBuilder()
        .setEmail("me@you.com")
        .setFirstName("Jay")
        .setLastName("Kreps")
        .setDob("01/01/1970")
        .setMailingAddress(
            Address.newBuilder()
                .setAddress1("42 Wallaby Way")
                .setCity("Mountain View")
                .setState("CA")
                .setPostalCode("90210")
                .build())
        .build()

    @Test
    fun testAdd() {

        val consumer = CustomerCommandConsumer(handler)

        every { handler.addCustomer(any()) } returns Customer.newBuilder(customer)
            .setId("1234")
            .build()

        consumer.processCustomerCommand(CustomerCommand.newBuilder()
            .setAction(CustomerAction.ADD)
            .setCustomer(customer)
            .build()
        )

        verify(exactly = 1) { handler.addCustomer(customer) }
        verify(exactly = 0) { handler.updateCustomer(any()) }
        verify(exactly = 0) { handler.deleteCustomer(any()) }
    }

    @Test
    fun testUpdate() {

        val consumer = CustomerCommandConsumer(handler)

        val updateCustomer = Customer.newBuilder(customer).setId("9876").build()

        every { handler.updateCustomer(any()) } returnsArgument 0

        consumer.processCustomerCommand(CustomerCommand.newBuilder()
            .setAction(CustomerAction.UPDATE)
            .setCustomer(updateCustomer)
            .build()
        )

        verify(exactly = 0) { handler.addCustomer(any()) }
        verify(exactly = 1) { handler.updateCustomer(updateCustomer) }
        verify(exactly = 0) { handler.deleteCustomer(any()) }
    }

    @Test
    fun testDelete() {

        val consumer = CustomerCommandConsumer(handler)

        val deleteCustomer = Customer.newBuilder(customer).setId("9876").build()

        every { handler.deleteCustomer(any()) } returns deleteCustomer.getId()

        consumer.processCustomerCommand(CustomerCommand.newBuilder()
            .setAction(CustomerAction.DELETE)
            .setCustomer(deleteCustomer)
            .build()
        )

        verify(exactly = 0) { handler.updateCustomer(any()) }
        verify(exactly = 0) { handler.addCustomer(any()) }
        verify(exactly = 1) { handler.deleteCustomer(deleteCustomer) }
    }
}