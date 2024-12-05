package io.confluent.devrel.order;

import io.confluent.devrel.event.OrderChangeEvent;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;

@Getter
@Serdeable
public class OrderChangeDto {

    private long orderId;
    private String eventType;

    public OrderChangeDto(OrderChangeEvent event) {
        this.orderId = event.getOrderId();
        this.eventType = event.getEventType().name();
    }

}
