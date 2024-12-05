package io.confluent.devrel.order;

import io.confluent.devrel.event.OrderChangeEvent;
import io.confluent.devrel.model.OrderItem;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@KafkaListener(groupId = "order-changes", value = "order-changes")
public class OrderChangeEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrderChangeEventListener.class);

    @Inject
    private OrderItemRepository orderItemRepository;

    @Topic("order-changes-avro")
    public void processOrderChangeEvent(OrderChangeEvent orderChangeEvent) {
        LOG.debug("Received order change event: {}", orderChangeEvent);
        switch (orderChangeEvent.getEventType()) {
            case DELETE_ITEM -> removeItem(orderChangeEvent);
            case ADD_ITEM -> addItem(orderChangeEvent);
            case UPDATE_ITEM_COUNT -> updateOrder(orderChangeEvent);
            default -> throw new IllegalStateException("Unexpected value: " + orderChangeEvent.getEventType());
        }
    }

    protected void removeItem(final OrderChangeEvent event) {
        LOG.info("Removing item {} from order {}", event.getItem().getProductId(), event.getOrderId());
        orderItemRepository.deleteByOrderIdAndProductId(event.getOrderId(), event.getItem().getProductId());
    }

    protected void addItem(final OrderChangeEvent event) {
        LOG.info("Adding item {} to order {}", event.getItem(), event.getOrderId());
        OrderItemDao orderItemDao = new OrderItemDao(null, event.getOrderId(),
                event.getItem().getProductId(), event.getItem().getDescription(),
                event.getItem().getQuantity(), event.getItem().getUnitPrice());

        LOG.debug("saving item {}", orderItemDao);
        orderItemRepository.save(orderItemDao);
    }

    protected void updateOrder(final OrderChangeEvent event) {
        LOG.info("Updating item {} for order {}", event.getItem(), event.getOrderId());

        Optional<OrderItemDao> orderItemDao = orderItemRepository.findByOrderIdAndProductId(
                event.getOrderId(),
                event.getItem().getProductId());
        if (orderItemDao.isPresent()) {
            LOG.info("Updating item {} for order {}", event.getItem(), event.getOrderId());
            OrderItemDao oi = orderItemDao.get();
            orderItemRepository.update(new OrderItemDao(oi.getId(), oi.getOrderId(),
                oi.getProductId(), oi.getDescription(),
                event.getItem().getQuantity(), oi.getUnitPrice()
            ));
        } else {
            LOG.info("Item {} for order {} not found, creating new one", event.getItem(), event.getOrderId());
            orderItemRepository.save(new OrderItemDao(null, event.getOrderId(),
                event.getItem().getProductId(), event.getItem().getDescription(),
                event.getItem().getQuantity(), event.getItem().getUnitPrice()));
        }
    }

}