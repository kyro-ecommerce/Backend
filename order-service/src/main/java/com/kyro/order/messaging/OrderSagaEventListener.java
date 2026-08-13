package com.kyro.order.messaging;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
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
    log.info(
        "Received StockResultEvent for Order ID {}: success={}", event.orderId(), event.success());
    orderRepository
        .findById(event.orderId())
        .ifPresent(
            order -> {
              if (order.getOrderStatus() != OrderStatus.PENDING) {
                log.info(
                    "Ignored stock result for terminal order {} in status {}",
                    event.orderId(),
                    order.getOrderStatus());
                return;
              }
              if (event.success()) {
                order.setStockReserved(true);
                if (canConfirm(order.getPaymentMethod(), order.getPaymentStatus())) {
                  order.setOrderStatus(OrderStatus.CONFIRMED);
                  log.info("Order ID {} successfully CONFIRMED via Saga.", event.orderId());
                } else {
                  log.info("Order ID {} reserved stock and is awaiting payment.", event.orderId());
                }
              } else {
                order.setStockReserved(false);
                order.setOrderStatus(OrderStatus.CANCELLED);
                log.warn(
                    "Order ID {} CANCELLED due to stock deduction failure: {}",
                    event.orderId(),
                    event.message());
              }
              orderRepository.save(order);
            });
  }

  static boolean canConfirm(PaymentMethod paymentMethod, PaymentStatus paymentStatus) {
    return paymentMethod == PaymentMethod.COD || paymentStatus == PaymentStatus.COMPLETED;
  }
}
