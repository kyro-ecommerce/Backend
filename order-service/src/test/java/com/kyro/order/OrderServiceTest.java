package com.kyro.order;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderServiceTest {

  @Test
  void purchaseCheckDelegatesWithoutLoadingOrderItems() {
    OrderService orderService =
        new OrderService(null, null, null, null, null, null) {
          @Override
          public boolean hasPurchasedAndDelivered(Long userId, Long productId) {
            return userId.equals(16L) && productId.equals(20L);
          }
        };

    assertTrue(new InternalOrderController(orderService).hasPurchasedAndDelivered(16L, 20L));
  }
}
