package com.kyro.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDTO {
  private Long productId;
  private String productName;
  private String productImageUrl;
  private int quantity;
  private int price;
  private String size;
  private Integer discountPercent;
  private Integer discountedPrice;
}
