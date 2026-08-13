package com.kyro.order.dto;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.Order;

public record OrderInternalResponse(
    Long id,
    Long userId,
    Integer totalDiscountedPrice,
    PaymentStatus paymentStatus,
    PaymentMethod paymentMethod,
    OrderStatus orderStatus) {
  public OrderInternalResponse(Order order) {
    this(
        order.getId(),
        order.getUserId(),
        order.getTotalDiscountedPrice(),
        order.getPaymentStatus(),
        order.getPaymentMethod(),
        order.getOrderStatus());
  }
}
