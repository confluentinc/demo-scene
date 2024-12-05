package io.confluent.devrel.product;


import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@MicronautTest(transactional = false)
@Property(name = "datasources.default.driver-class-name", value = "org.testcontainers.jdbc.ContainerDatabaseDriver")
@Property(name = "datasources.default.url", value = "jdbc:tc:mysql:8:///db")
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductPriceChangedEventHandlerTest extends BaseProductTest {

    @Test
    void shouldHandleProductPriceChanged(ProductPriceChangesClient client, ProductRepository productRepository) {
        Product p = new Product(null, "P100", "Product One", BigDecimal.TEN);

        Long id = productRepository.save(p).getId();

        ProductPriceChangedEvent event = new ProductPriceChangedEvent("P100", new BigDecimal("14.50"));
        client.sendProductPriceChange(event.productCode(), event);

        await().pollInterval(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Optional<Product> optionalProduct = productRepository.findByCode("P100");
                    assertThat(optionalProduct).isPresent();
                    assertThat(optionalProduct.get().getCode()).isEqualTo("P100");
                    assertThat(optionalProduct.get().getPrice()).isEqualTo(new BigDecimal("14.50"));
                });
    }
}
