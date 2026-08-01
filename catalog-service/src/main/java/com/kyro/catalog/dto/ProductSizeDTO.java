package com.kyro.catalog.dto;

import com.kyro.catalog.ProductSize;

public class ProductSizeDTO {
  private String name;
  private Integer quantity;

  public ProductSizeDTO() {}

  public ProductSizeDTO(ProductSize productSize) {
    this.name = productSize.getName();
    this.quantity = productSize.getQuantity();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}
