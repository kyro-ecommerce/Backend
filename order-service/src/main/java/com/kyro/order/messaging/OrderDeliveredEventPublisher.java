package com.kyro.order.messaging;

import com.kyro.order.config.RabbitMQConfig;
import com.kyro.order.event.OrderDeliveredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderDeliveredEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(OrderDeliveredEventPublisher.class);
  private final RabbitTemplate rabbitTemplate;

  public OrderDeliveredEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void publish(OrderDeliveredEvent event) {
    try {
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_DELIVERED_ROUTING_KEY, event);
    } catch (RuntimeException exception) {
      log.error("Failed to publish OrderDeliveredEvent for order {}", event.orderId(), exception);
    }
  }
}
