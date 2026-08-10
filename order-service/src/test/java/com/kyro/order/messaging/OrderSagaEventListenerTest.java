package com.kyro.order.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

class OrderSagaEventListenerTest {

  @Test
  void confirmsCodOrCompletedPaymentOnly() {
    assertTrue(OrderSagaEventListener.canConfirm(PaymentMethod.COD, PaymentStatus.PENDING));
    assertFalse(OrderSagaEventListener.canConfirm(PaymentMethod.VNPAY, PaymentStatus.PENDING));
    assertTrue(OrderSagaEventListener.canConfirm(PaymentMethod.VNPAY, PaymentStatus.COMPLETED));
  }
}
