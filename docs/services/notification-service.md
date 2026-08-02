# 🔔 Notification Service Documentation

> **Service Name**: `notification-service`  
> **Port**: `8084`  
> **Database**: None (Stateless Event Consumer)  
> **Integration**: JavaMailSender (SMTP), RabbitMQ (`auth.exchange`, `order.exchange`)  
> **Package**: `com.kyro`

---

## 📌 1. Chức Năng Chính

**Notification Service** đóng vai trò là một Consumer bất đồng bộ xử lý việc gửi email thông báo cho người dùng mà không làm nghẽn luồng xử lý chính:

1. **Gửi Mail OTP Xác Thực Tài Khoản & Khôi Phục Mật Khẩu**:
   - Lắng nghe RabbitMQ Queue `otp.email.queue`.
   - Render HTML Email Template chứa mã xác thực 6 chữ số và đồng hồ đếm ngược 10 phút.
   - Gửi email qua SMTP server (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`).
2. **Gửi Mail Xác Nhận Đơn Hàng (Order Confirmation Email)**:
   - Lắng nghe RabbitMQ Queue `order.email.queue`.
   - Render bảng chi tiết hóa đơn: tên sản phẩm, số lượng, giá tiền, địa chỉ giao hàng và phương thức thanh toán.
3. **Template HTML Chuyên Nghiệp**:
   - Tích hợp Thymeleaf Engine để render các mẫu email HTML chuẩn responsive, vừa vặn trên điện thoại và máy tính.

---

## 🐰 2. RabbitMQ Listeners (Event Consumers)

```java
@Component
@Slf4j
public class EmailNotificationConsumer {

    @Autowired
    private MailService mailService;

    @RabbitListener(queues = "otp.email.queue")
    public void consumeOtpEmailEvent(OtpEmailEvent event) {
        log.info("Received OTP email event for email: {}", event.getEmail());
        mailService.sendOtpEmail(event.getEmail(), event.getOtp(), event.getType());
    }

    @RabbitListener(queues = "order.email.queue")
    public void consumeOrderEmailEvent(OrderCreatedEvent event) {
        log.info("Received Order Confirmation email event for order: {}", event.getOrderNumber());
        mailService.sendOrderConfirmationEmail(event);
    }
}
```

---

## 📧 3. Cấu Hồi Môi Trường SMTP

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```
