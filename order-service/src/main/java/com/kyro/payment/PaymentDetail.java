package com.kyro.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.Order;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_details")
public class PaymentDetail {

  public PaymentDetail() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "order_id")
  @JsonIgnore
  private Order order;

  @Column(name = "payment_method")
  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  @Column(name = "payment_status")
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  @Column(name = "transaction_id", length = 100)
  private String transactionId;

  @Column(name = "total_amount")
  private int totalAmount;

  @Column(name = "payment_log", columnDefinition = "TEXT")
  private String paymentLog;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Column(name = "vnp_ResponseCode")
  private String vnp_ResponseCode; // Response code from VNPay ("00" indicates success)

  @Column(name = "vnp_SecureHash")
  private String vnp_SecureHash; // Integrity check hash from VNPay

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
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

  public LocalDateTime getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(LocalDateTime paymentDate) {
    this.paymentDate = paymentDate;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public int getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(int totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getPaymentLog() {
    return paymentLog;
  }

  public void setPaymentLog(String paymentLog) {
    this.paymentLog = paymentLog;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getVnp_ResponseCode() {
    return vnp_ResponseCode;
  }

  public void setVnp_ResponseCode(String vnp_ResponseCode) {
    this.vnp_ResponseCode = vnp_ResponseCode;
  }

  public String getVnp_SecureHash() {
    return vnp_SecureHash;
  }

  public void setVnp_SecureHash(String vnp_SecureHash) {
    this.vnp_SecureHash = vnp_SecureHash;
  }
}
