package com.kyro.cart.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
  private String userId;
  private List<CartItemDTO> items = new ArrayList<>();
  private BigDecimal totalAmount = BigDecimal.ZERO;

  public void calculateTotalAmount() {
    this.totalAmount =
        items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
