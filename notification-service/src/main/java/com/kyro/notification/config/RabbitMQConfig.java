package com.kyro.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration class for RabbitMQ queues, exchanges, and message converters. */
@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE = "notification-exchange";
  public static final String OTP_QUEUE = "otp-queue";
  public static final String OTP_ROUTING_KEY = "notification.otp";
  public static final String ORDER_QUEUE = "order-queue";
  public static final String ORDER_ROUTING_KEY = "notification.order";

  @Bean
  public Queue otpQueue() {
    return new Queue(OTP_QUEUE, true);
  }

  @Bean
  public Queue orderQueue() {
    return new Queue(ORDER_QUEUE, true);
  }

  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(EXCHANGE);
  }

  @Bean
  public Binding otpBinding() {
    return BindingBuilder.bind(otpQueue()).to(exchange()).with(OTP_ROUTING_KEY);
  }

  @Bean
  public Binding orderBinding() {
    return BindingBuilder.bind(orderQueue()).to(exchange()).with(ORDER_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
