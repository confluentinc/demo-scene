package io.confluent.devrel.product;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    @Query("update Product p set p.price = :price where p.code = :productCode")
    void updateProductPrice(String productCode, BigDecimal price);

}

