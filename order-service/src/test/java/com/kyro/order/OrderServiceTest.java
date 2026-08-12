package com.kyro.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kyro.enums.OrderStatus;
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

  @Test
  void statusUpdateUsesLifecycleTransitionAndRejectsPendingReset() {
    Order order = new Order();
    order.setOrderStatus(OrderStatus.PENDING);
    OrderService orderService =
        new OrderService(null, null, null, null, null, null) {
          @Override
          public Order findOrderById(Long orderId) {
            return order;
          }

          @Override
          public Order confirmedOrder(Long orderId) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            return order;
          }
        };

    assertEquals(
        OrderStatus.CONFIRMED,
        orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED).getOrderStatus());
    assertThrows(
        IllegalArgumentException.class,
        () -> orderService.updateOrderStatus(1L, OrderStatus.PENDING));
  }
}
