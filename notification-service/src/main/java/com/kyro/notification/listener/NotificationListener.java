package com.kyro.notification.listener;

import com.kyro.notification.config.RabbitMQConfig;
import com.kyro.notification.service.EmailService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Listener to consume notification events from RabbitMQ queues. */
@Component
public class NotificationListener {

  private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

  private final EmailService emailService;

  public NotificationListener(EmailService emailService) {
    this.emailService = emailService;
  }

  /**
   * Consumes OTP send request.
   *
   * @param payload map containing email, otp, and expiration time
   */
  @RabbitListener(queues = RabbitMQConfig.OTP_QUEUE)
  public void receiveOtpNotification(Map<String, Object> payload) {
    log.info("Received OTP notification request: {}", payload);
    try {
      String email = (String) payload.get("email");
      String otp = (String) payload.get("otp");
      int expirationMinutes = (Integer) payload.get("expirationMinutes");

      emailService.sendOtpEmail(email, otp, expirationMinutes);
    } catch (Exception e) {
      log.error("Error processing OTP notification payload: {}", e.getMessage(), e);
    }
  }

  /**
   * Consumes order confirmation send request.
   *
   * @param payload map containing email and order details
   */
  @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
  public void receiveOrderNotification(Map<String, Object> payload) {
    log.info("Received Order confirmation email request");
    try {
      String email = (String) payload.get("email");
      Map<String, Object> orderData = (Map<String, Object>) payload.get("order");

      emailService.sendOrderMail(email, orderData);
    } catch (Exception e) {
      log.error("Error processing Order confirmation payload: {}", e.getMessage(), e);
    }
  }
}
