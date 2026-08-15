package com.kyro.order.event;

import java.util.List;

public record OrderDeliveredEvent(Long orderId, List<Item> items) {
  public record Item(Long productId, int quantity) {}
}
