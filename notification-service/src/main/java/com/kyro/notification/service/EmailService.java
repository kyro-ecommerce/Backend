package com.kyro.notification.service;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Service for constructing and sending HTML emails asynchronously. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${app.company.logo.url:https://kyro.com/logo.png}")
  private String companyLogoUrl;

  /**
   * Sends verification OTP code email.
   *
   * @param email recipient email address
   * @param otp one-time password code
   * @param otpExpirationMinutes expiration limit in minutes
   */
  public void sendOtpEmail(String email, String otp, int otpExpirationMinutes) {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    try {
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      Context context = new Context();
      context.setVariable("otpCode", otp);
      context.setVariable("otpExpirationMinutes", otpExpirationMinutes);
      context.setVariable("companyLogoUrl", companyLogoUrl);

      String htmlContent = templateEngine.process("mail/otp-verification-email", context);

      helper.setTo(email);
      helper.setSubject("Mã xác thực tài khoản của bạn");
      helper.setText(htmlContent, true);

      mailSender.send(mimeMessage);
      log.info("Successfully sent OTP email to {}", email);

    } catch (Exception e) {
      log.error("Failed to send OTP email to {} due to error: {}", email, e.getMessage(), e);
    }
  }

  /**
   * Sends order confirmation email.
   *
   * @param email recipient email address
   * @param orderData order details representation
   */
  public void sendOrderMail(String email, Map<String, Object> orderData) {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    try {
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      Context context = new Context();
      context.setVariable("order", orderData);
      context.setVariable("companyLogoUrl", companyLogoUrl);

      String htmlContent = templateEngine.process("mail/order-confirmation-email", context);

      Object orderId = orderData.get("id");
      helper.setTo(email);
      helper.setSubject("Xác nhận đơn hàng TechShop #" + orderId);
      helper.setText(htmlContent, true);

      mailSender.send(mimeMessage);
      log.info("Successfully sent order confirmation email to {} for order #{}", email, orderId);

    } catch (Exception e) {
      log.error(
          "Failed to send order confirmation email to {} due to error: {}",
          email,
          e.getMessage(),
          e);
    }
  }
}
