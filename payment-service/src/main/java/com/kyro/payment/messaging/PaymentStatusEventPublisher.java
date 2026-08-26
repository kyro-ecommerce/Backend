package com.kyro.payment.messaging;

import com.kyro.payment.config.RabbitMQConfig;
import com.kyro.payment.event.PaymentStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentStatusEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(PaymentStatusEventPublisher.class);

  private final RabbitTemplate rabbitTemplate;

  public PaymentStatusEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publish(PaymentStatusChangedEvent event) {
    // ponytail: after-commit publishing can lose an event on broker failure; add an outbox when
    // retries matter.
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.PAYMENT_EXCHANGE, RabbitMQConfig.PAYMENT_STATUS_UPDATED_ROUTING_KEY, event);
    log.info("Published payment status {} for order {}", event.status(), event.orderId());
  }
}
