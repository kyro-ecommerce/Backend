package com.kyro.payment;

import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing payment details for transactions. Decoupled from Order entity by storing
 * plain orderId.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
  private String vnp_ResponseCode;

  @Column(name = "vnp_SecureHash")
  private String vnp_SecureHash;
}
