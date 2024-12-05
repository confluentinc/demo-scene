package io.confluent.devrel.order;

import io.confluent.devrel.event.OrderChangeEvent;
import io.confluent.devrel.event.OrderChangeEventType;
import io.confluent.devrel.model.OrderItem;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.datafaker.providers.base.Options;

import java.util.UUID;

@Controller("/orders")
public class OrderController {

    @Inject
    private OrderChangeClient orderChangeClient;

    @Put("/change/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<OrderChangeDto> putOrderChange(@PathVariable Long orderId, @Body OrderChangeEvent event) {
        orderChangeClient.sendOrderChange(orderId, event);
        return HttpResponse.created(new OrderChangeDto(event));
    }

    @Put("/random")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<OrderChangeDto> randomOrderChangeEvent() {
        OrderChangeEvent event = generate();
        orderChangeClient.sendOrderChange(event.getOrderId(), event);
        return HttpResponse.created(new OrderChangeDto(event));
    }

    private OrderChangeEvent generate() {
        Faker faker = new Faker();
        Options types = faker.options();

        final long orderId = faker.number().numberBetween(1L, 999999L);
        OrderItem item = OrderItem.newBuilder()
                .setOrderId(orderId)
                .setProductId(UUID.randomUUID().toString())
                .setDescription(faker.device().modelName())
                .setQuantity(faker.random().nextInt(1, 10))
                .setUnitPrice(faker.number().randomDouble(2, 9, 100))
                .build();

        return OrderChangeEvent.newBuilder()
                .setOrderId(orderId)
                .setEventType(types.option(OrderChangeEventType.class))
                .setItem(item)
                .build();
    }
}
