package io.confluent.devrel.product;


import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@MicronautTest(transactional = false, propertySources = {"classpath:application-test.yml"})
@Property(name = "datasources.default.driver-class-name", value = "org.testcontainers.jdbc.ContainerDatabaseDriver")
@Property(name = "datasources.default.url", value = "jdbc:tc:mysql:8:///db")
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductControllerTest extends BaseProductTest {

    @AfterEach
    public void cleanupDB(ProductRepository productRepository) {
        productRepository.deleteAll();
    }

    @Test
    public void testGetAllProducts(RequestSpecification spec, ProductRepository productRepository) {
        List<Product> products = List.of(
                new Product(null, "P100", "Product 100", new BigDecimal("19.99")),
                new Product(null, "P200", "Product 200", new BigDecimal("29.99"))
        );

        List<Product> saveResults = productRepository.saveAll(products);
        Response response = spec.when().get("/products");
        response.then().statusCode(200);

        List<Product> returnProducts = response.getBody().jsonPath().getList(".", Product.class);
        assertEquals(products.size(), returnProducts.size());
    }

    @Test
    public void testGetById(RequestSpecification spec, ProductRepository productRepository) {
        Product savedProduct = productRepository.save(new Product(null, "P800", "Product 800", new BigDecimal("89.99")));
        Product resultProduct =
                spec.when().get("/products/{id}", savedProduct.getId()).then().statusCode(200)
                        .extract().body().jsonPath().getObject(".", Product.class);

        assertEquals(savedProduct, resultProduct);
    }

    @Test
    public void testPutPriceChangeEvent(RequestSpecification spec, ProductRepository productRepository, ProductPriceChangesClient client) {
        Product savedProduct = productRepository.save(new Product(null, "P900", "Product 900", new BigDecimal("99.99")));
        final BigDecimal newPrice = new BigDecimal("999.99");

        ProductPriceChangedEvent event = new ProductPriceChangedEvent(savedProduct.getCode(), newPrice);

        spec.given()
                .header("Content-Type", "application/json")
                .body(event)
                .when()
                .post("/products/priceChange/{code}", savedProduct.getCode())
                .then()
                .statusCode(201);

        await().pollInterval(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            var product = spec.given().pathParam("id", savedProduct.getId())
                    .when().get("/products/{id}", savedProduct.getId())
                    .then().statusCode(200)
                    .extract().body().jsonPath().getObject(".", Product.class);
            assertNotEquals(savedProduct.getPrice(), product.getPrice());
            assertEquals(newPrice, product.getPrice());
        });
    }
}
