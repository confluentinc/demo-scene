package io.confluent;

import io.micronaut.http.annotation.*;
import io.micronaut.http.HttpResponse;
import javax.inject.Inject;

@Controller("/order")
public class OrderController {
    @Inject
    private OrderProducer producer;

    @Post
    public HttpResponse placeOrder(@Body NewOrder newOrder) {
        producer.sendOrder("", newOrder);
        return HttpResponse.ok();
    }

}