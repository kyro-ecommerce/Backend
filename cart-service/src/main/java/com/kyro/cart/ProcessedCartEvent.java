package com.kyro.cart;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed_cart_events")
public class ProcessedCartEvent {
  @Id private Long orderId;
  protected ProcessedCartEvent() {}
  public ProcessedCartEvent(Long orderId) { this.orderId = orderId; }
}
