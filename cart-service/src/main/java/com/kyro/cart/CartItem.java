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
  @Column(name = "product_id", nullable = false) private Long productId;
  @Column(name = "product_name", nullable = false) private String productName;
  @Column(name = "product_image_url") private String productImageUrl;
  @Column(nullable = false) private int quantity;
  @Column(nullable = false) private int price;
  private String size;
  @Column(name = "discount_percent") private Integer discountPercent;
  @Column(name = "discounted_price") private Integer discountedPrice;
  @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

  @PrePersist @PreUpdate void touch() { updatedAt = LocalDateTime.now(); }
  public Long getId() { return id; }
  public Cart getCart() { return cart; }
  public void setCart(Cart cart) { this.cart = cart; }
  public Long getProductId() { return productId; }
  public void setProductId(Long productId) { this.productId = productId; }
  public String getProductName() { return productName; }
  public void setProductName(String productName) { this.productName = productName; }
  public String getProductImageUrl() { return productImageUrl; }
  public void setProductImageUrl(String value) { productImageUrl = value; }
  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { this.quantity = quantity; }
  public int getPrice() { return price; }
  public void setPrice(int price) { this.price = price; }
  public String getSize() { return size; }
  public void setSize(String size) { this.size = size; }
  public Integer getDiscountPercent() { return discountPercent; }
  public void setDiscountPercent(Integer value) { discountPercent = value; }
  public Integer getDiscountedPrice() { return discountedPrice; }
  public void setDiscountedPrice(Integer value) { discountedPrice = value; }
}
