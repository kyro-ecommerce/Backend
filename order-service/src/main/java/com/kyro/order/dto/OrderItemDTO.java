package com.kyro.order.dto;

import com.kyro.order.OrderItem;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object representing details of an item in an order. */
@Data
@NoArgsConstructor
public class OrderItemDTO {
  private Long id;
  private Long productId;
  private String productTitle;
  private String imageUrl;
  private int quantity;
  private String size;
  private int price;
  private Integer discountedPrice;
  private Integer discountPercent;
  private LocalDateTime deliveryDate;

  public OrderItemDTO(OrderItem orderItem) {
    this.id = orderItem.getId();
    this.productId = orderItem.getProductId();
    this.productTitle = orderItem.getProductName();
    this.imageUrl = orderItem.getProductImageUrl();
    this.quantity = orderItem.getQuantity();
    this.size = orderItem.getSize();
    this.price = orderItem.getPrice();
    this.discountedPrice = orderItem.getDiscountedPrice();
    this.discountPercent = orderItem.getDiscountPercent();
    this.deliveryDate = orderItem.getDeliveryDate();
  }
}
