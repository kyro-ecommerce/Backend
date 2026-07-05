package com.kyro.catalog.dto;

import com.kyro.catalog.ProductSize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class ProductSizeDTO {
  private String name;
  private Integer quantity;

  public ProductSizeDTO(ProductSize productSize) {
    this.name = productSize.getName();
    this.quantity = productSize.getQuantity();
  }
}
