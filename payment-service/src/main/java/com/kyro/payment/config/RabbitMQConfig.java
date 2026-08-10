package com.kyro.payment.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String PAYMENT_EXCHANGE = "payment-exchange";
  public static final String PAYMENT_STATUS_UPDATED_ROUTING_KEY = "payment.status.updated";

  @Bean
  public TopicExchange paymentExchange() {
    return new TopicExchange(PAYMENT_EXCHANGE);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
