package io.confluent.devrel.product;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient(value = "product-changes")
public interface ProductPriceChangesClient {

    @Topic("product-price-changes")
    void sendProductPriceChange(@KafkaKey String productCode, ProductPriceChangedEvent event);

    @Topic("product-create")
    void sendCreateProduct(Product product);
}

