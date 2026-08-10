package com.kyro.order.dto;

import com.kyro.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record CreateOrderRequest(
    @NotNull Long addressId,
    PaymentMethod paymentMethod,
    @NotEmpty List<@NotNull @Positive Long> cartItemIds,
    @PositiveOrZero long cartVersion,
    @PositiveOrZero int expectedTotalDiscountedPrice) {
  public CreateOrderRequest {
    paymentMethod = paymentMethod == null ? PaymentMethod.COD : paymentMethod;
  }
}
