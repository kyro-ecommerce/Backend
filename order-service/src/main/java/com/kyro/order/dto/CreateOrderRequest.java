package com.kyro.order.dto;

import com.kyro.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(@NotNull Long addressId, PaymentMethod paymentMethod) {
  public CreateOrderRequest {
    paymentMethod = paymentMethod == null ? PaymentMethod.COD : paymentMethod;
  }
}
