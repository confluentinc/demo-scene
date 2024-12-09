package io.confluent.devrel.order;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends CrudRepository<OrderItemDao, Long> {

    List<OrderItemDao> findByOrderId(Long orderId);

    Optional<OrderItemDao> findByOrderIdAndProductId(Long orderId, String productId);

    void deleteByOrderIdAndProductId(Long orderId, String productId);

}
