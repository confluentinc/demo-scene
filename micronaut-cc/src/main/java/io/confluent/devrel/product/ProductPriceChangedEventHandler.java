package io.confluent.devrel.product;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(offsetReset = OffsetReset.EARLIEST, groupId = "demo")
public class ProductPriceChangedEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProductPriceChangedEventHandler.class);

    private final ProductRepository productRepository;

    ProductPriceChangedEventHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Topic("product-price-changes")
    public void handle(ProductPriceChangedEvent event) {
        LOG.info("Received product price change with code :{}", event.productCode());
        productRepository.updateProductPrice(event.productCode(), event.price());
    }

    @Topic("product-create")
    public void addProduct(Product product) {
        productRepository.save(product);
    }
}

