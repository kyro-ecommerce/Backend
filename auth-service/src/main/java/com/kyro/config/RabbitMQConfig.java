package com.kyro.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration class for RabbitMQ event publisher. */
@Configuration
public class RabbitMQConfig {

  public static final String EXCHANGE = "notification-exchange";
  public static final String OTP_ROUTING_KEY = "notification.otp";
  public static final String ORDER_ROUTING_KEY = "notification.order";

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
