package com.kyro.order.dto;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.Order;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data Transfer Object representing order details. */
@Data
@NoArgsConstructor
public class OrderDTO {
  private Long id;
  private OrderStatus orderStatus;
  private Integer totalDiscountedPrice;
  private int discount;
  private int totalItems;
  private LocalDateTime orderDate;
  private LocalDateTime deliveryDate;
  private Integer originalPrice;
  private AddressDTO shippingAddress;
  private PaymentStatus paymentStatus;
  private List<OrderItemDTO> orderItems;
  private PaymentMethod paymentMethod;

  public OrderDTO(Order order) {
    this.id = order.getId();
    this.originalPrice = order.getOriginalPrice();
    this.orderStatus = order.getOrderStatus();
    this.totalDiscountedPrice =
        order.getTotalDiscountedPrice() != null ? order.getTotalDiscountedPrice() : 0;
    this.discount = order.getDiscount();
    this.totalItems = order.getTotalItems();
    this.orderDate = order.getOrderDate();
    this.deliveryDate = order.getDeliveryDate();
    this.shippingAddress =
        order.getShippingAddress() != null ? new AddressDTO(order.getShippingAddress()) : null;
    this.paymentStatus = order.getPaymentStatus();
    this.orderItems = new ArrayList<>();
    this.paymentMethod = order.getPaymentMethod();
    if (order.getOrderItems() != null) {
      order
          .getOrderItems()
          .forEach(
              item -> {
                if (item != null) {
                  this.orderItems.add(new OrderItemDTO(item));
                }
              });
    }
  }
}
