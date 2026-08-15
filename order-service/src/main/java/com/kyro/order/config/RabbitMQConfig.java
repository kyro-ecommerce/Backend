package com.kyro.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration class for Order Service RabbitMQ queues and exchange. */
@Configuration
public class RabbitMQConfig {

  public static final String ORDER_EXCHANGE = "order-exchange";
  public static final String PAYMENT_EXCHANGE = "payment-exchange";
  public static final String NOTIFICATION_EXCHANGE = "notification-exchange";

  public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
  public static final String ORDER_DELIVERED_ROUTING_KEY = "order.delivered";
  public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";
  public static final String STOCK_FAILED_ROUTING_KEY = "stock.failed";

  public static final String ORDER_SAGA_QUEUE = "order-saga-queue";
  public static final String PAYMENT_STATUS_QUEUE = "order-payment-status-queue";
  public static final String PAYMENT_STATUS_UPDATED_ROUTING_KEY = "payment.status.updated";
  public static final String ORDER_NOTIFICATION_ROUTING_KEY = "notification.order";

  @Bean
  public TopicExchange orderExchange() {
    return new TopicExchange(ORDER_EXCHANGE);
  }

  @Bean
  public Queue orderSagaQueue() {
    return new Queue(ORDER_SAGA_QUEUE, true);
  }

  @Bean
  public TopicExchange paymentExchange() {
    return new TopicExchange(PAYMENT_EXCHANGE);
  }

  @Bean
  public Queue paymentStatusQueue() {
    return new Queue(PAYMENT_STATUS_QUEUE, true);
  }

  @Bean
  public Binding paymentStatusBinding() {
    return BindingBuilder.bind(paymentStatusQueue())
        .to(paymentExchange())
        .with(PAYMENT_STATUS_UPDATED_ROUTING_KEY);
  }

  @Bean
  public Binding stockReservedBinding() {
    return BindingBuilder.bind(orderSagaQueue())
        .to(orderExchange())
        .with(STOCK_RESERVED_ROUTING_KEY);
  }

  @Bean
  public Binding stockFailedBinding() {
    return BindingBuilder.bind(orderSagaQueue()).to(orderExchange()).with(STOCK_FAILED_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
