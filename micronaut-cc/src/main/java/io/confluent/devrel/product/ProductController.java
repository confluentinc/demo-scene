package io.confluent.devrel.product;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Controller("/products")
public class ProductController {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private ProductPriceChangesClient productPriceChangesClient;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<Product>> getAllProducts() {
        return HttpResponse.ok(productRepository.findAll());
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id).map(HttpResponse::ok).orElse(HttpResponse.notFound());
    }

    @Post("/priceChange/{code}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<?> changePrice(@PathVariable String code, @Body ProductPriceChangedEvent productPriceChangedEvent) {
        productPriceChangesClient.sendProductPriceChange(code, productPriceChangedEvent);
        return HttpResponse.created(productPriceChangedEvent);
    }

    @Post("/random")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Product> createProduct() {
        Faker faker = new Faker();
        Product product = new Product(
                null, UUID.randomUUID().toString(), faker.device().modelName(),
                BigDecimal.valueOf(faker.random().nextDouble(0,100)).setScale(2, RoundingMode.DOWN));
        productPriceChangesClient.sendCreateProduct(product);
        return HttpResponse.created(product);
    }
}
