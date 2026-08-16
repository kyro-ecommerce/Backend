package com.kyro.order.dto;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.Order;
import java.time.Instant;

public record OrderInternalResponse(
    Long id,
    Long userId,
    Long totalDiscountedPrice,
    PaymentStatus paymentStatus,
    PaymentMethod paymentMethod,
    OrderStatus orderStatus,
    Instant expiresAt) {
  public OrderInternalResponse(Order order) {
    this(
        order.getId(),
        order.getUserId(),
        order.getTotalDiscountedPrice(),
        order.getPaymentStatus(),
        order.getPaymentMethod(),
        order.getOrderStatus(),
        order.getExpiresAt());
  }
}
