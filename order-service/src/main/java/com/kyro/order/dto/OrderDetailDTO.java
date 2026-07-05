package com.kyro.order.dto;

import com.kyro.order.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Detailed Order DTO decoupled from auth domain packages. */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailDTO extends OrderDTO {
  private Long userId;

  public OrderDetailDTO(Order order) {
    super(order);
    this.userId = order.getUserId();
  }
}
