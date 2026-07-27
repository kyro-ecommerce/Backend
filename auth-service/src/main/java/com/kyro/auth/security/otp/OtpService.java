package com.kyro.auth.security.otp;

import com.kyro.auth.User;
import com.kyro.auth.UserRepository;
import com.kyro.config.RabbitMQConfig;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Service for generating, validating, and publishing OTP requests to RabbitMQ. */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

  private final RabbitTemplate rabbitTemplate;
  private final UserRepository userRepository;

  @Value("${app.otp.expiration-minutes:10}")
  private int otpExpirationMinutes;

  @Value("${app.otp.resend-cooldown-minutes}")
  private int resendCooldownMinutes;

  // In-memory OTP storage (email -> [otp, expirationTime, generationTime])
  private final Map<String, OtpData> otpStorage = new HashMap<>();

  /**
   * Generates a random 6-digit OTP code and caches it.
   *
   * @param email user email address
   * @return generated OTP code
   */
  public String generateOtp(String email) {
    SecureRandom random = new SecureRandom();
    int otp = 100000 + random.nextInt(900000);
    String otpString = String.valueOf(otp);

    otpStorage.put(
        email,
        new OtpData(
            otpString, LocalDateTime.now().plusMinutes(otpExpirationMinutes), LocalDateTime.now()));

    log.info("[OTP GENERATED] Email: {}, OTP: {}", email, otpString);
    return otpString;
  }

  /**
   * Validates OTP code for the user email and activates the account if valid.
   *
   * @param email user email address
   * @param otp OTP code
   * @return true if OTP is valid and not expired, false otherwise
   */
  public boolean validateOtp(String email, String otp) {
    OtpData otpData = otpStorage.get(email);

    if (otpData == null) {
      return false;
    }

    boolean isValid =
        otpData.getOtp().equals(otp) && LocalDateTime.now().isBefore(otpData.getExpirationTime());

    if (isValid) {
      User user = userRepository.findByEmail(email);

      if (user != null && user.isBanned()) {
        throw new RuntimeException("Your account is banned");
      }

      if (user != null && !user.isActive() && !user.isBanned()) {
        activateUserAccount(email);
      }
      otpStorage.remove(email);
    } else if (LocalDateTime.now().isAfter(otpData.getExpirationTime())) {
      otpStorage.remove(email);
    }

    return isValid;
  }

  /**
   * Checks if OTP resend cooldown has elapsed.
   *
   * @param email user email address
   * @return true if allowed to resend, false otherwise
   */
  public boolean isResendAllowed(String email) {
    OtpData existingOtpData = otpStorage.get(email);
    if (existingOtpData == null) {
      return true;
    }
    LocalDateTime allowedResendTime =
        existingOtpData.getGenerationTime().plusMinutes(resendCooldownMinutes);

    return LocalDateTime.now().isAfter(allowedResendTime);
  }

  /**
   * Gets remaining resend cooldown time in seconds.
   *
   * @param email user email address
   * @return remaining seconds
   */
  public long getRemainingCooldownSeconds(String email) {
    OtpData existingOtpData = otpStorage.get(email);
    if (existingOtpData == null) {
      return 0;
    }
    LocalDateTime lastSentTime = existingOtpData.getGenerationTime();
    LocalDateTime allowedResendTime = lastSentTime.plusMinutes(resendCooldownMinutes);
    LocalDateTime now = LocalDateTime.now();

    if (now.isBefore(allowedResendTime)) {
      return Duration.between(now, allowedResendTime).getSeconds();
    }
    return 0;
  }

  /**
   * Publishes an OTP email event to RabbitMQ for asynchronous delivery.
   *
   * @param email user email address
   * @param otp generated OTP code
   */
  public void sendOtpEmail(String email, String otp) {
    try {
      Map<String, Object> payload =
          Map.of(
              "email", email,
              "otp", otp,
              "expirationMinutes", otpExpirationMinutes);
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.EXCHANGE, RabbitMQConfig.OTP_ROUTING_KEY, payload);
      log.info("Published OTP email send event to RabbitMQ for {}", email);
    } catch (Exception e) {
      log.error("Failed to publish OTP email event for {} due to error: {}", email, e.getMessage());
      log.warn("[DEV MODE] ĐỂ PHỤC VỤ TEST/DEVELOPMENT, ĐÂY LÀ MÃ OTP CỦA BẠN: >>> {} <<<", otp);
    }
  }

  private void activateUserAccount(String email) {
    User user = userRepository.findByEmail(email);
    if (user != null) {
      user.setActive(true);
      userRepository.save(user);
    }
  }

  private static class OtpData {
    private final String otp;
    private final LocalDateTime expirationTime;
    private final LocalDateTime generationTime;

    public OtpData(String otp, LocalDateTime expirationTime, LocalDateTime generationTime) {
      this.otp = otp;
      this.expirationTime = expirationTime;
      this.generationTime = generationTime;
    }

    public String getOtp() { return otp; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public LocalDateTime getGenerationTime() { return generationTime; }
  }
}
