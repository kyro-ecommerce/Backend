package com.kyro.cart.dto;

import java.util.ArrayList;
import java.util.List;

public class CartDTO {
  private String userId;
  private long version;
  private List<CartItemDTO> items = new ArrayList<>();
  private long totalPrice = 0;
  private long totalSalePrice = 0;

  public CartDTO() {}

  public CartDTO(
      String userId, long version, List<CartItemDTO> items, long totalPrice, long totalSalePrice) {
    this.userId = userId;
    this.version = version;
    this.items = items != null ? items : new ArrayList<>();
    this.totalPrice = totalPrice;
    this.totalSalePrice = totalSalePrice;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public List<CartItemDTO> getItems() {
    return items;
  }

  public void setItems(List<CartItemDTO> items) {
    this.items = items;
  }

  public long getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(long totalPrice) {
    this.totalPrice = totalPrice;
  }

  public long getTotalSalePrice() {
    return totalSalePrice;
  }

  public void setTotalSalePrice(long totalSalePrice) {
    this.totalSalePrice = totalSalePrice;
  }

  public void calculateTotalAmount() {
    this.totalPrice = items.stream().mapToLong(item -> item.getPrice() * item.getQuantity()).sum();
    this.totalSalePrice =
        items.stream().mapToLong(item -> item.getSalePrice() * item.getQuantity()).sum();
  }
}
