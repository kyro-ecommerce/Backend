package com.kyro.cart;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "variant_id", nullable = false)
  private Long variantId;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(nullable = false)
  private String sku;

  @Column(name = "variant_name", nullable = false)
  private String variantName;

  @Column(name = "product_image_url")
  private String productImageUrl;

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false)
  private long price;

  @Column(name = "sale_price", nullable = false)
  private long salePrice;

  @Column(name = "discount_percent", nullable = false)
  private int discountPercent;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  void touch() {
    updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Cart getCart() {
    return cart;
  }

  public void setCart(Cart v) {
    cart = v;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long v) {
    productId = v;
  }

  public Long getVariantId() {
    return variantId;
  }

  public void setVariantId(Long v) {
    variantId = v;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String v) {
    productName = v;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String v) {
    sku = v;
  }

  public String getVariantName() {
    return variantName;
  }

  public void setVariantName(String v) {
    variantName = v;
  }

  public String getProductImageUrl() {
    return productImageUrl;
  }

  public void setProductImageUrl(String v) {
    productImageUrl = v;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int v) {
    quantity = v;
  }

  public long getPrice() {
    return price;
  }

  public void setPrice(long v) {
    price = v;
  }

  public long getSalePrice() {
    return salePrice;
  }

  public void setSalePrice(long v) {
    salePrice = v;
  }

  public int getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(int v) {
    discountPercent = v;
  }
}
