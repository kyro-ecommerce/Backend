package com.kyro.order;

import com.kyro.auth.User;
import com.kyro.catalog.Product;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price")
    private int price;

    @Column(name = "size")
    private String size;

    @Column(name = "discounted_price")
    private Integer discountedPrice;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    // Helper method to get user through order
    public User getUser() {
        return order != null ? order.getUser() : null;
    }
}