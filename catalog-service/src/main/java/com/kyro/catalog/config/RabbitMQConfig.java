package com.kyro.catalog.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String ORDER_EXCHANGE = "order-exchange";
  public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

  public static final String CATALOG_ORDER_QUEUE = "catalog-order-created-queue";

  @Bean
  public TopicExchange orderExchange() {
    return new TopicExchange(ORDER_EXCHANGE);
  }

  @Bean
  public Queue catalogOrderQueue() {
    return new Queue(CATALOG_ORDER_QUEUE, true);
  }

  @Bean
  public Binding catalogOrderBinding() {
    return BindingBuilder.bind(catalogOrderQueue()).to(orderExchange()).with(ORDER_CREATED_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
