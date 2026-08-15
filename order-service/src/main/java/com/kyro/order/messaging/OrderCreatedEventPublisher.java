package com.kyro.order.messaging;

import com.kyro.order.config.RabbitMQConfig;
import com.kyro.order.event.OrderConfirmedEvent;
import com.kyro.order.event.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderCreatedEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventPublisher.class);
  private final RabbitTemplate rabbitTemplate;

  public OrderCreatedEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publish(OrderCreatedEvent event) {
    try {
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, event);
    } catch (RuntimeException e) {
      log.error("Failed to publish OrderCreatedEvent for order {}", event.orderId(), e);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publishConfirmation(OrderConfirmedEvent event) {
    try {
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.NOTIFICATION_EXCHANGE,
          RabbitMQConfig.ORDER_NOTIFICATION_ROUTING_KEY,
          event.payload());
    } catch (RuntimeException e) {
      log.error("Failed to publish order confirmation email event", e);
    }
  }
}
