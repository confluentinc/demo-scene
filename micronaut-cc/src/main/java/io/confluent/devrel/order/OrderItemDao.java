package io.confluent.devrel.order;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "order_item")
public class OrderItemDao {

    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "product_id", nullable = false)
    private String productId;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private Integer quantity;
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    public OrderItemDao() {
    }

    public OrderItemDao(Long id, Long orderId, String productId, String description, Integer quantity, Double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemDao that = (OrderItemDao) o;
        return Objects.equals(id, that.id) && Objects.equals(orderId, that.orderId) && Objects.equals(productId, that.productId) && Objects.equals(description, that.description) && Objects.equals(quantity, that.quantity) && Objects.equals(unitPrice, that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, productId, description, quantity, unitPrice);
    }
}
