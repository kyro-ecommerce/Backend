package com.kyro.payment;

import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing payment details for transactions. Decoupled from Order entity by storing
 * plain orderId.
 */
@Entity
@Table(name = "payment_details")
public class PaymentDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

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

  @Column(name = "vnp_ResponseCode")
  private String vnp_ResponseCode;

  @Column(name = "vnp_SecureHash")
  private String vnp_SecureHash;

  public PaymentDetail() {}

  public PaymentDetail(
      Long id,
      Long orderId,
      PaymentMethod paymentMethod,
      PaymentStatus paymentStatus,
      LocalDateTime paymentDate,
      String transactionId,
      int totalAmount,
      String paymentLog,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      String vnp_ResponseCode,
      String vnp_SecureHash) {
    this.id = id;
    this.orderId = orderId;
    this.paymentMethod = paymentMethod;
    this.paymentStatus = paymentStatus;
    this.paymentDate = paymentDate;
    this.transactionId = transactionId;
    this.totalAmount = totalAmount;
    this.paymentLog = paymentLog;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.vnp_ResponseCode = vnp_ResponseCode;
    this.vnp_SecureHash = vnp_SecureHash;
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
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
