package com.kyro.cart.dto;

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
  private int totalPrice = 0;
  private int totalDiscountedPrice = 0;

  public void calculateTotalAmount() {
    this.totalPrice =
        items.stream()
            .mapToInt(item -> item.getPrice() * item.getQuantity())
            .sum();
    this.totalDiscountedPrice =
        items.stream()
            .mapToInt(item -> (item.getDiscountedPrice() != null ? item.getDiscountedPrice() : item.getPrice()) * item.getQuantity())
            .sum();
  }
}
