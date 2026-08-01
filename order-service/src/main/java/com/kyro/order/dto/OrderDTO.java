package com.kyro.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.Order;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Data Transfer Object representing order details. */
public class OrderDTO {
  private Long id;
  private OrderStatus orderStatus;
  private Integer totalDiscountedPrice;
  private int discount;
  private int totalItems;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime orderDate;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime deliveryDate;

  private Integer originalPrice;
  private AddressDTO shippingAddress;
  private PaymentStatus paymentStatus;
  private List<OrderItemDTO> orderItems;
  private PaymentMethod paymentMethod;

  public OrderDTO() {}

  public OrderDTO(Order order) {
    this.id = order.getId();
    this.originalPrice = order.getOriginalPrice();
    this.orderStatus = order.getOrderStatus();
    this.totalDiscountedPrice =
        order.getTotalDiscountedPrice() != null ? order.getTotalDiscountedPrice() : 0;
    this.discount = order.getDiscount();
    this.totalItems = order.getTotalItems();
    this.orderDate = order.getOrderDate();
    this.deliveryDate = order.getDeliveryDate();
    this.shippingAddress =
        order.getShippingAddress() != null ? new AddressDTO(order.getShippingAddress()) : null;
    this.paymentStatus = order.getPaymentStatus();
    this.orderItems = new ArrayList<>();
    this.paymentMethod = order.getPaymentMethod();
    if (order.getOrderItems() != null) {
      order
          .getOrderItems()
          .forEach(
              item -> {
                if (item != null) {
                  this.orderItems.add(new OrderItemDTO(item));
                }
              });
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OrderStatus getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(OrderStatus orderStatus) {
    this.orderStatus = orderStatus;
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

  public LocalDateTime getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDateTime orderDate) {
    this.orderDate = orderDate;
  }

  public LocalDateTime getDeliveryDate() {
    return deliveryDate;
  }

  public void setDeliveryDate(LocalDateTime deliveryDate) {
    this.deliveryDate = deliveryDate;
  }

  public Integer getOriginalPrice() {
    return originalPrice;
  }

  public void setOriginalPrice(Integer originalPrice) {
    this.originalPrice = originalPrice;
  }

  public AddressDTO getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(AddressDTO shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public PaymentStatus getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public List<OrderItemDTO> getOrderItems() {
    return orderItems;
  }

  public void setOrderItems(List<OrderItemDTO> orderItems) {
    this.orderItems = orderItems;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }
}
