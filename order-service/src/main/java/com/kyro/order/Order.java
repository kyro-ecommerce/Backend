package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Table(name = "orders")
public class Order {

  public Order() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "user_email")
  private String userEmail;

  @Column(name = "order_date")
  private LocalDateTime orderDate;

  @Column(name = "original_price", precision = 19, scale = 2)
  private int originalPrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_status")
  private OrderStatus orderStatus;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private Collection<OrderItem> orderItems = new HashSet<>();

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "orderAddress")
  private Address shippingAddress;

  @Column(name = "delivery_date")
  private LocalDateTime deliveryDate;

  @Column(name = "total_discounted_price")
  private Integer totalDiscountedPrice;

  @Column(name = "discount")
  private int discount;

  @Column(name = "total_items")
  private int totalItems;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method")
  @ColumnDefault("'COD'")
  private PaymentMethod paymentMethod;

  @Column(name = "payment_status")
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public LocalDateTime getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDateTime orderDate) {
    this.orderDate = orderDate;
  }

  public int getOriginalPrice() {
    return originalPrice;
  }

  public void setOriginalPrice(int originalPrice) {
    this.originalPrice = originalPrice;
  }

  public OrderStatus getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(OrderStatus orderStatus) {
    this.orderStatus = orderStatus;
  }

  public Collection<OrderItem> getOrderItems() {
    return orderItems;
  }

  public void setOrderItems(Collection<OrderItem> orderItems) {
    this.orderItems = orderItems;
  }

  public Address getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(Address shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public LocalDateTime getDeliveryDate() {
    return deliveryDate;
  }

  public void setDeliveryDate(LocalDateTime deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public Integer getTotalDiscountedPrice() {
    return totalDiscountedPrice;
  }

  public void setTotalDiscountedPrice(Integer totalDiscountedPrice) {
    this.totalDiscountedPrice = totalDiscountedPrice;
  }

  public int getDiscount() {
    return discount;
  }

  public void setDiscount(int discount) {
    this.discount = discount;
  }

  public int getTotalItems() {
    return totalItems;
  }

  public void setTotalItems(int totalItems) {
    this.totalItems = totalItems;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }
}
