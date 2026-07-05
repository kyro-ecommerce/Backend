package com.kyro.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity representing an item inside an order. Stores product details as denormalized fields. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_item")
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "product_name")
  private String productName;

  @Column(name = "product_image_url")
  private String productImageUrl;

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

  public Long getUserId() {
    return order != null ? order.getUserId() : null;
  }
}
