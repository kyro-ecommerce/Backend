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

  public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
  public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";
  public static final String STOCK_FAILED_ROUTING_KEY = "stock.failed";

  public static final String ORDER_SAGA_QUEUE = "order-saga-queue";

  @Bean
  public TopicExchange orderExchange() {
    return new TopicExchange(ORDER_EXCHANGE);
  }

  @Bean
  public Queue orderSagaQueue() {
    return new Queue(ORDER_SAGA_QUEUE, true);
  }

  @Bean
  public Binding stockReservedBinding() {
    return BindingBuilder.bind(orderSagaQueue()).to(orderExchange()).with(STOCK_RESERVED_ROUTING_KEY);
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
