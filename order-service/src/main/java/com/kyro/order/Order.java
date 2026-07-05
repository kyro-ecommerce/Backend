package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.payment.PaymentDetail;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

/** Entity representing an order in the system. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name = "orders")
public class Order {

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

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private PaymentDetail paymentDetails;

  @ManyToOne(cascade = CascadeType.ALL)
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
}
