package io.confluent.devrel;

import io.confluent.devrel.event.OrderChangeEvent;
import io.confluent.devrel.event.OrderChangeEventType;
import io.confluent.devrel.model.OrderItem;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;

@SerdeImport(OrderChangeEvent.class)
@SerdeImport(OrderChangeEventType.class)
@SerdeImport(OrderItem.class)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}