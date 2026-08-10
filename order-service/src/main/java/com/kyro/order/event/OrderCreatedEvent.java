package com.kyro.order.event;

import java.util.List;

public record OrderCreatedEvent(
    Long orderId, Long userId, String userEmail, List<OrderItemEvent> items) {

  public record OrderItemEvent(Long cartItemId, Long productId, String size, int quantity, int price) {}
}
