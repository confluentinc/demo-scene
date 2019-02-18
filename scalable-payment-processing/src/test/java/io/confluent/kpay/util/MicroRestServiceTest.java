package io.confluent.kpay.util;

import io.confluent.kpay.payments.model.Payment;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

public class MicroRestServiceTest {

    @Test
    public void shouldGetPayment() throws Exception {

        SimplePaymentImpl instance = new SimplePaymentImpl();

        MicroRestService service = new MicroRestService();
        service.start(instance, "localhost", 19999);

        Client client = ClientBuilder.newClient();
        Payment result = client.target("http://localhost:19999").path("/state/get")
                .request(MediaType.APPLICATION_JSON)
                .get(Payment.class);

        service.stop();

        Assert.assertTrue(result.getId().equals("payment-id"));
    }


    public static class SimplePaymentImpl implements SimpleInterface<Payment> {

        public String keys() {
            return "yay";
        }

        public Payment get() {
            return new Payment("txn", "payment-id", "from", "to", 123, Payment.State.incoming);
        }
    }

    @Path("state")
    public interface SimpleInterface<V> {

        @GET()
        @Path("/keys")
        @Produces(MediaType.APPLICATION_JSON)
        String keys();

        @GET()
        @Path("/get")
        @Produces(MediaType.APPLICATION_JSON)
        V get();
    }

}