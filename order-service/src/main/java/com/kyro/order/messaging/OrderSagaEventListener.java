package com.kyro.order.messaging;

import com.kyro.enums.OrderStatus;
import com.kyro.order.Order;
import com.kyro.order.OrderRepository;
import com.kyro.order.config.RabbitMQConfig;
import com.kyro.order.event.StockResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaEventListener {

  private static final Logger log = LoggerFactory.getLogger(OrderSagaEventListener.class);

  private final OrderRepository orderRepository;

  public OrderSagaEventListener(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @RabbitListener(queues = RabbitMQConfig.ORDER_SAGA_QUEUE)
  public void handleStockResult(StockResultEvent event) {
    log.info("Received StockResultEvent for Order ID {}: success={}", event.orderId(), event.success());
    orderRepository
        .findById(event.orderId())
        .ifPresent(
            order -> {
              if (event.success()) {
                order.setOrderStatus(OrderStatus.CONFIRMED);
                log.info("Order ID {} successfully CONFIRMED via Saga.", event.orderId());
              } else {
                order.setOrderStatus(OrderStatus.CANCELLED);
                log.warn("Order ID {} CANCELLED due to stock deduction failure: {}", event.orderId(), event.message());
              }
              orderRepository.save(order);
            });
  }
}
