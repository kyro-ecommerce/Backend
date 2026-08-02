package com.kyro.cart.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String ORDER_EXCHANGE = "order-exchange";
  public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";

  public static final String CART_CLEAR_QUEUE = "cart-clear-queue";

  @Bean
  public TopicExchange orderExchange() {
    return new TopicExchange(ORDER_EXCHANGE);
  }

  @Bean
  public Queue cartClearQueue() {
    return new Queue(CART_CLEAR_QUEUE, true);
  }

  @Bean
  public Binding cartClearBinding() {
    return BindingBuilder.bind(cartClearQueue()).to(orderExchange()).with(STOCK_RESERVED_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
