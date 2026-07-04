package com.kyro.auth.security.otp;

import com.kyro.auth.User;
import com.kyro.auth.UserRepository;
import com.kyro.order.Order;



import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException; // Import

import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Value("${app.otp.resend-cooldown-minutes}")
    private int resendCooldownMinutes;

    @Value("${app.company.logo.url}")
    private String companyLogoUrl;

    // Lưu trữ OTP và thời gian hết hạn (email -> [otp, expirationTime])
    private final Map<String, OtpData> otpStorage = new HashMap<>();

    /**
     * Tạo OTP ngẫu nhiên 6 số
     */
    public String generateOtp(String email) {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        String otpString = String.valueOf(otp);

        otpStorage.put(email, new OtpData(
                otpString,
                LocalDateTime.now().plusMinutes(otpExpirationMinutes),
                LocalDateTime.now()
        ));

        return otpString;
    }

    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);

        if (otpData == null) {
            return false;
        }

        boolean isValid = otpData.getOtp().equals(otp) &&
                LocalDateTime.now().isBefore(otpData.getExpirationTime());

        if (isValid) {
            User user = userRepository.findByEmail(email);

            if (user != null && user.isBanned()) {
                throw new RuntimeException("your account is banned");
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

    public boolean isResendAllowed(String email) {
        OtpData existingOtpData = otpStorage.get(email);
        if (existingOtpData == null) {
            return true; // No previous OTP sent recently, so allowed
        }
        // Calculate the earliest time a resend is allowed
        LocalDateTime allowedResendTime = existingOtpData.getGenerationTime()
                .plusMinutes(resendCooldownMinutes);

        // Check if the current time is after the allowed resend time
        return LocalDateTime.now().isAfter(allowedResendTime);
    }

    /// --- New Helper: Get remaining cooldown seconds ---
    public long getRemainingCooldownSeconds(String email) {
        OtpData existingOtpData = otpStorage.get(email);
        if (existingOtpData == null) {
            return 0;
        }
        LocalDateTime lastSentTime = existingOtpData.getGenerationTime();
        LocalDateTime allowedResendTime = lastSentTime.plusMinutes(resendCooldownMinutes);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(allowedResendTime)) {
            // Calculate remaining duration and return seconds
            return Duration.between(now, allowedResendTime).getSeconds();
        }
        return 0; // Cooldown has passed
    }

    public void sendOtpEmail(String email, String otp) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            // true = multipart message
            // true = enable HTML content
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Chuẩn bị context cho Thymeleaf
            Context context = new Context();
            context.setVariable("otpCode", otp);
            context.setVariable("otpExpirationMinutes", otpExpirationMinutes);
            context.setVariable("companyLogoUrl", companyLogoUrl);
            // context.setVariable("userEmail", email); // Có thể thêm nếu cần hiển thị email trong template

            // Xử lý template
            String htmlContent = templateEngine.process("mail/otp-verification-email", context);

            helper.setTo(email);
            helper.setSubject("Mã xác thực tài khoản của bạn"); // Chủ đề email
            helper.setText(htmlContent, true); // true để chỉ định đây là nội dung HTML

            mailSender.send(mimeMessage);
            log.info("Đã gửi email OTP HTML tới {}", email);

        } catch (Exception e) {
            log.error("LỖI GỬI EMAIL: Không thể gửi email OTP tới {} do lỗi: {}", email, e.getMessage());
            log.warn("[DEV MODE] ĐỂ PHỤC VỤ TEST/DEVELOPMENT, ĐÂY LÀ MÃ OTP CỦA BẠN: >>> {} <<<", otp);
            // Không ném ngoại lệ nữa, cho phép đăng ký tiếp tục ở môi trường test
        }
    }

    private void activateUserAccount(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
        }
    }


    @Getter
    @AllArgsConstructor
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expirationTime;
        private final LocalDateTime generationTime;
    }

    public void sendOrderMail(String email, Order order) {
        if (order == null) {
            log.error("Cannot send email for null order");
            throw new RuntimeException("Order not found for sending email.");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Chuẩn bị context cho Thymeleaf
            Context context = new Context();
            context.setVariable("order", order); // Truyền đối tượng Order
            context.setVariable("companyLogoUrl", companyLogoUrl); // Truyền URL logo
            // Bạn có thể thêm các biến khác nếu cần trong template

            // Xử lý template
            String htmlContent = templateEngine.process("mail/order-confirmation-email", context); // Đường dẫn đến file template

            helper.setTo(email);
            helper.setSubject("Xác nhận đơn hàng TechShop #" + order.getId());
            helper.setText(htmlContent, true); // true để chỉ định đây là nội dung HTML

            mailSender.send(mimeMessage);
            log.info("Successfully sent order confirmation email to {} for order #{}", email, order.getId());

        } catch (MessagingException e) {
            log.error("MessagingException while sending order confirmation email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email thông báo đơn hàng do lỗi gửi thư.", e);
        } catch (Exception e) {
            log.error("Unexpected exception while sending order confirmation email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Có lỗi không mong muốn xảy ra khi gửi email thông báo đơn hàng.", e);
        }
    }

}