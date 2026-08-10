package com.kyro.order.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kyro.enums.PaymentStatus;
import org.junit.jupiter.api.Test;

class PaymentStatusEventListenerTest {

  @Test
  void mapsPaymentEventStatus() {
    assertEquals(PaymentStatus.COMPLETED, PaymentStatusEventListener.toPaymentStatus("COMPLETED"));
  }
}
