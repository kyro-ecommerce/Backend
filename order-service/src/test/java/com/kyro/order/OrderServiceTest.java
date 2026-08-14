package com.kyro.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.exceptions.DomainException;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

  @Test
  void purchaseCheckDelegatesWithoutLoadingOrderItems() {
    OrderService orderService =
        new OrderService(null, null, null, null, null, null, null) {
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
        new OrderService(null, null, null, null, null, null, null) {
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
    DomainException exception =
        assertThrows(
            DomainException.class, () -> orderService.updateOrderStatus(1L, OrderStatus.PENDING));
    assertEquals(409, exception.getStatus().value());
    assertEquals("INVALID_ORDER_STATE", exception.getErrorCode());
  }

  @Test
  void confirmsVnpayOnlyAfterStockAndNeverRevivesCancelledOrder() {
    Order order = new Order();
    order.setPaymentMethod(PaymentMethod.VNPAY);
    order.setPaymentStatus(PaymentStatus.PENDING);
    order.setOrderStatus(OrderStatus.PENDING);

    OrderService.applyPaymentStatus(order, PaymentStatus.COMPLETED);
    assertEquals(OrderStatus.PENDING, order.getOrderStatus());

    order.setStockReserved(true);
    OrderService.applyPaymentStatus(order, PaymentStatus.COMPLETED);
    assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());

    order.setOrderStatus(OrderStatus.CANCELLED);
    OrderService.applyPaymentStatus(order, PaymentStatus.COMPLETED);
    assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
  }

  @Test
  void cancellingPaidVnpayOrderDoesNotClaimRefund() {
    Order order = new Order();
    order.setPaymentMethod(PaymentMethod.VNPAY);
    order.setPaymentStatus(PaymentStatus.COMPLETED);
    order.setOrderStatus(OrderStatus.PENDING);

    OrderService.applyCancellationPaymentStatus(order);

    assertEquals(PaymentStatus.COMPLETED, order.getPaymentStatus());
  }

  @Test
  void paymentFailureWaitsForStockThenRestoresExactlyOnce() {
    Order order = order(PaymentStatus.PENDING, false);
    assertFalse(OrderService.applyPaymentStatus(order, PaymentStatus.FAILED));
    assertEquals(OrderStatus.PENDING, order.getOrderStatus());
    assertTrue(OrderService.applyStockResult(order, true));
    assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());
    assertFalse(OrderService.applyStockResult(order, true));
  }

  @Test
  void successEventsConfirmInEitherOrderAndLateFailureCannotRevive() {
    Order paymentFirst = order(PaymentStatus.PENDING, false);
    OrderService.applyPaymentStatus(paymentFirst, PaymentStatus.COMPLETED);
    OrderService.applyStockResult(paymentFirst, true);
    assertEquals(OrderStatus.CONFIRMED, paymentFirst.getOrderStatus());

    Order stockFirst = order(PaymentStatus.PENDING, false);
    OrderService.applyStockResult(stockFirst, true);
    OrderService.applyPaymentStatus(stockFirst, PaymentStatus.COMPLETED);
    assertEquals(OrderStatus.CONFIRMED, stockFirst.getOrderStatus());

    stockFirst.setOrderStatus(OrderStatus.CANCELLED);
    OrderService.applyStockResult(stockFirst, true);
    assertEquals(OrderStatus.CANCELLED, stockFirst.getOrderStatus());
  }

  private static Order order(PaymentStatus paymentStatus, boolean stockReserved) {
    OrderItem item = new OrderItem();
    item.setProductId(3L);
    item.setSize("M");
    item.setQuantity(2);
    Order order = new Order();
    order.setPaymentMethod(PaymentMethod.VNPAY);
    order.setPaymentStatus(paymentStatus);
    order.setOrderStatus(OrderStatus.PENDING);
    order.setStockReserved(stockReserved);
    order.setOrderItems(java.util.List.of(item));
    return order;
  }
}
