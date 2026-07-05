package com.kyro.cart.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
  private Long productId;
  private String productName;
  private int quantity;
  private BigDecimal price;
  private String imageUrl;
}
