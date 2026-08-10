package com.kyro.cart.dto;

import java.util.ArrayList;
import java.util.List;

public class CartDTO {
  private String userId;
  private long version;
  private List<CartItemDTO> items = new ArrayList<>();
  private int totalPrice = 0;
  private int totalDiscountedPrice = 0;

  public CartDTO() {}

  public CartDTO(String userId, long version, List<CartItemDTO> items, int totalPrice, int totalDiscountedPrice) {
    this.userId = userId;
    this.version = version;
    this.items = items != null ? items : new ArrayList<>();
    this.totalPrice = totalPrice;
    this.totalDiscountedPrice = totalDiscountedPrice;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public long getVersion() { return version; }
  public void setVersion(long version) { this.version = version; }

  public List<CartItemDTO> getItems() {
    return items;
  }

  public void setItems(List<CartItemDTO> items) {
    this.items = items;
  }

  public int getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(int totalPrice) {
    this.totalPrice = totalPrice;
  }

  public int getTotalDiscountedPrice() {
    return totalDiscountedPrice;
  }

  public void setTotalDiscountedPrice(int totalDiscountedPrice) {
    this.totalDiscountedPrice = totalDiscountedPrice;
  }

  public void calculateTotalAmount() {
    this.totalPrice = items.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();
    this.totalDiscountedPrice =
        items.stream()
            .mapToInt(
                item ->
                    (item.getDiscountedPrice() != null
                            ? item.getDiscountedPrice()
                            : item.getPrice())
                        * item.getQuantity())
            .sum();
  }
}
