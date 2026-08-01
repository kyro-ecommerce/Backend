package com.kyro.cart.dto;

public class CartItemDTO {
  private Long productId;
  private String productName;
  private String productImageUrl;
  private int quantity;
  private int price;
  private String size;
  private Integer discountPercent;
  private Integer discountedPrice;

  public CartItemDTO() {}

  public CartItemDTO(
      Long productId,
      String productName,
      String productImageUrl,
      int quantity,
      int price,
      String size,
      Integer discountPercent,
      Integer discountedPrice) {
    this.productId = productId;
    this.productName = productName;
    this.productImageUrl = productImageUrl;
    this.quantity = quantity;
    this.price = price;
    this.size = size;
    this.discountPercent = discountPercent;
    this.discountedPrice = discountedPrice;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public String getProductImageUrl() {
    return productImageUrl;
  }

  public void setProductImageUrl(String productImageUrl) {
    this.productImageUrl = productImageUrl;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public Integer getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(Integer discountPercent) {
    this.discountPercent = discountPercent;
  }

  public Integer getDiscountedPrice() {
    return discountedPrice;
  }

  public void setDiscountedPrice(Integer discountedPrice) {
    this.discountedPrice = discountedPrice;
  }
}
