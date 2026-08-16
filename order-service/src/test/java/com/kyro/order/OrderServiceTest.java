package com.kyro.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.exceptions.DomainException;
import com.kyro.order.client.CatalogClient;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.Optional;
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

  @Test
  void vnpayPublishesOnceForEitherSuccessEventOrder() {
    Order paymentFirst = order(PaymentStatus.PENDING, false);
    boolean wasPending = paymentFirst.getOrderStatus() == OrderStatus.PENDING;
    OrderService.applyPaymentStatus(paymentFirst, PaymentStatus.COMPLETED);
    assertFalse(OrderService.isNewlyConfirmed(wasPending, paymentFirst));
    OrderService.applyStockResult(paymentFirst, true);
    assertTrue(OrderService.isNewlyConfirmed(wasPending, paymentFirst));

    Order stockFirst = order(PaymentStatus.PENDING, false);
    wasPending = stockFirst.getOrderStatus() == OrderStatus.PENDING;
    OrderService.applyStockResult(stockFirst, true);
    assertFalse(OrderService.isNewlyConfirmed(wasPending, stockFirst));
    OrderService.applyPaymentStatus(stockFirst, PaymentStatus.COMPLETED);
    assertTrue(OrderService.isNewlyConfirmed(wasPending, stockFirst));
  }

  @Test
  void duplicateCodStockEventDoesNotPublishTwice() {
    Order order = order(PaymentStatus.PENDING, false);
    order.setPaymentMethod(PaymentMethod.COD);
    boolean firstWasPending = order.getOrderStatus() == OrderStatus.PENDING;
    OrderService.applyStockResult(order, true);
    assertTrue(OrderService.isNewlyConfirmed(firstWasPending, order));

    boolean duplicateWasPending = order.getOrderStatus() == OrderStatus.PENDING;
    OrderService.applyStockResult(order, true);
    assertFalse(OrderService.isNewlyConfirmed(duplicateWasPending, order));
  }

  @Test
  void failuresDoNotPublishConfirmation() {
    Order order = order(PaymentStatus.PENDING, false);
    boolean wasPending = order.getOrderStatus() == OrderStatus.PENDING;
    OrderService.applyStockResult(order, false);
    assertFalse(OrderService.isNewlyConfirmed(wasPending, order));
  }

  @Test
  void assignsExpirationOnlyToVnpayOrders() {
    Instant createdAt = Instant.parse("2026-08-16T08:00:00Z");

    assertEquals(
        createdAt.plusSeconds(15 * 60),
        OrderService.expirationFor(PaymentMethod.VNPAY, createdAt));
    assertEquals(null, OrderService.expirationFor(PaymentMethod.COD, createdAt));
  }

  @Test
  void expiresReservedVnpayOrderAfterGraceCutoff() {
    Instant expiration = Instant.parse("2026-08-16T08:15:00Z");
    Order order = order(PaymentStatus.PENDING, true);
    order.setId(42L);
    order.setExpiresAt(expiration);
    order.getOrderItems().iterator().next().setVariantId(7L);
    OrderRepository repository = repositoryFor(order);
    boolean[] adjusted = {false};
    CatalogClient catalog =
        proxy(
            CatalogClient.class,
            (instance, method, arguments) -> {
              adjusted[0] = true;
              assertEquals(7L, arguments[0]);
              assertEquals(
                  new CatalogClient.StockAdjustmentRequest(7L, 2), arguments[1]);
              return null;
            });
    OrderService service = new OrderService(repository, null, catalog, null, null, null);

    assertTrue(service.expireVnpayOrder(42L, expiration));

    assertTrue(adjusted[0]);
    assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    assertEquals(PaymentStatus.CANCELLED, order.getPaymentStatus());
    assertFalse(order.isStockReserved());
  }

  @Test
  void skipsPaidOrNotYetExpiredOrders() {
    Instant expiration = Instant.parse("2026-08-16T08:15:00Z");
    Order pending = order(PaymentStatus.PENDING, true);
    pending.setExpiresAt(expiration);
    Order paid = order(PaymentStatus.COMPLETED, true);
    paid.setExpiresAt(expiration);

    assertFalse(
        OrderService.isExpiredVnpayOrder(
            pending,
            VnpayOrderExpirationScheduler.expirationCutoff(
                expiration.plusSeconds(5 * 60 - 1))));
    assertTrue(
        OrderService.isExpiredVnpayOrder(
            pending,
            VnpayOrderExpirationScheduler.expirationCutoff(
                expiration.plusSeconds(5 * 60))));
    assertFalse(OrderService.isExpiredVnpayOrder(paid, expiration.plusSeconds(1)));
  }

  @Test
  void stockFailureLeavesOrderPendingForRetry() {
    Instant expiration = Instant.parse("2026-08-16T08:15:00Z");
    Order order = order(PaymentStatus.PENDING, true);
    order.setId(42L);
    order.setExpiresAt(expiration);
    order.getOrderItems().iterator().next().setVariantId(7L);
    OrderRepository repository = repositoryFor(order);
    CatalogClient catalog =
        proxy(
            CatalogClient.class,
            (instance, method, arguments) -> {
              throw new IllegalStateException("catalog unavailable");
            });
    OrderService service = new OrderService(repository, null, catalog, null, null, null);

    assertThrows(
        IllegalStateException.class, () -> service.expireVnpayOrder(42L, expiration));

    assertEquals(OrderStatus.PENDING, order.getOrderStatus());
    assertEquals(PaymentStatus.PENDING, order.getPaymentStatus());
    assertTrue(order.isStockReserved());
  }

  @Test
  void expiresOrderWithoutCallingCatalogWhenStockWasNotReserved() {
    Instant expiration = Instant.parse("2026-08-16T08:15:00Z");
    Order order = order(PaymentStatus.PENDING, false);
    order.setId(42L);
    order.setExpiresAt(expiration);
    OrderService service =
        new OrderService(repositoryFor(order), null, null, null, null, null);

    assertTrue(service.expireVnpayOrder(42L, expiration));

    assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    assertEquals(PaymentStatus.CANCELLED, order.getPaymentStatus());
  }

  @Test
  void identifiesLateSuccessWithoutRevivingCancelledOrder() {
    Order order = order(PaymentStatus.CANCELLED, false);
    order.setId(42L);
    order.setOrderStatus(OrderStatus.CANCELLED);
    OrderService service =
        new OrderService(repositoryFor(order), null, null, null, null, null);

    service.updatePaymentStatus(42L, PaymentStatus.COMPLETED);

    assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
    assertEquals(PaymentStatus.CANCELLED, order.getPaymentStatus());
  }

  private static OrderRepository repositoryFor(Order order) {
    return proxy(
        OrderRepository.class,
        (instance, method, arguments) ->
            switch (method.getName()) {
              case "findByIdForUpdate" -> Optional.of(order);
              case "save" -> arguments[0];
              default -> throw new UnsupportedOperationException(method.getName());
            });
  }

  @SuppressWarnings("unchecked")
  private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
    return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler);
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
