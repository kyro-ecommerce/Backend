package com.kyro.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kyro.order.OrderItem;
import java.time.LocalDateTime;

/** Data Transfer Object representing details of an item in an order. */
public class OrderItemDTO {
  private Long id;
  private Long productId;
  private Long variantId;
  private String sku;
  private String productTitle;
  private String imageUrl;
  private int quantity;
  private String size;
  private long price;
  private Long discountedPrice;
  private Integer discountPercent;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime deliveryDate;

  public OrderItemDTO() {}

  public OrderItemDTO(OrderItem orderItem) {
    this.id = orderItem.getId();
    this.productId = orderItem.getProductId();
    this.variantId = orderItem.getVariantId();
    this.sku = orderItem.getSku();
    this.productTitle = orderItem.getProductName();
    this.imageUrl = orderItem.getProductImageUrl();
    this.quantity = orderItem.getQuantity();
    this.size = orderItem.getSize();
    this.price = orderItem.getPrice();
    this.discountedPrice = orderItem.getDiscountedPrice();
    this.discountPercent = orderItem.getDiscountPercent();
    this.deliveryDate = orderItem.getDeliveryDate();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getProductTitle() {
    return productTitle;
  }

  public void setProductTitle(String productTitle) {
    this.productTitle = productTitle;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  public long getPrice() {
    return price;
  }

  public void setPrice(long price) {
    this.price = price;
  }

  public Long getDiscountedPrice() {
    return discountedPrice;
  }

  public void setDiscountedPrice(Long discountedPrice) {
    this.discountedPrice = discountedPrice;
  }

  public Long getVariantId() {
    return variantId;
  }

  public void setVariantId(Long value) {
    variantId = value;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String value) {
    sku = value;
  }

  public Integer getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(Integer discountPercent) {
    this.discountPercent = discountPercent;
  }

  public LocalDateTime getDeliveryDate() {
    return deliveryDate;
  }

  public void setDeliveryDate(LocalDateTime deliveryDate) {
    this.deliveryDate = deliveryDate;
  }
}
