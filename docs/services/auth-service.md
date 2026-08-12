# 🔐 Auth Service Documentation

> **Service Name**: `auth-service`  
> **Port**: `8081`  
> **Database**: PostgreSQL (`kyro_auth`)  
> **Framework**: Spring Boot 3.3.2, Spring Security, Spring OAuth2 Client, Java 21  
> **Package**: `com.kyro`

---

## 📌 1. Chức Năng Chính

**Auth Service** chịu trách nhiệm toàn bộ vòng đời xác thực, ủy quyền và quản lý tài khoản người dùng:

1. **Đăng Ký & Đăng Nhập Local**:
   - Mã hóa mật khẩu bằng **BCryptPasswordEncoder**.
   - Sinh chuỗi JWT Access Token (hạn 1 giờ) và Refresh Token (hạn 24 giờ).
2. **Xác Thực OAuth2**:
   - Đăng nhập bằng Google & GitHub.
   - Tự động sync thông tin user profile và tạo tài khoản mới nếu chưa tồn tại.
3. **Quản Lý OTP Khôi Phục & Xác Minh**:
   - Sinh mã OTP 6 chữ số ngẫu nhiên.
   - Phát sự kiện bất đồng bộ sang RabbitMQ (`auth.exchange` -> `otp.email.queue`) để `notification-service` gửi mail.
   - Giới hạn cooldown resend OTP (1 phút) và thời gian sống OTP (10 phút).
4. **Quản Lý Địa Chỉ Giao Hàng (Addresses)**:
   - Thêm, sửa, xóa, đặt địa chỉ mặc định cho người dùng.

---

## 🗄️ 2. Entities & Cấu Trúc Bảng Database

- **`User`** (`users`): `id`, `email`, `password`, `fullName`, `phone`, `role` (`ROLE_USER`, `ROLE_ADMIN`), `provider` (`LOCAL`, `GOOGLE`, `GITHUB`), `enabled`.
- **`Address`** (`addresses`): `id`, `userId`, `recipientName`, `phoneNumber`, `streetAddress`, `ward`, `district`, `city`, `isDefault`.
- **`RefreshToken`** (`refresh_tokens`): `id`, `userId`, `token`, `expiryDate`, `revoked`.

---

## 📡 3. Danh Sách REST Endpoints Chính

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản người dùng mới | Public |
| `POST` | `/api/v1/auth/login` | Đăng nhập nhận JWT Access & Refresh Token | Public |
| `POST` | `/api/v1/auth/verification` | Xác thực OTP đăng ký | Public |
| `POST` | `/api/v1/auth/verification/resend` | Gửi lại OTP | Public |
| `POST` | `/api/v1/auth/refresh` | Sinh Access Token mới từ Refresh Token | Public |
| `POST` | `/api/v1/auth/password-reset` | Đổi mật khẩu mới bằng OTP | Public |
| `GET/PATCH` | `/api/v1/users/me` | Đọc/cập nhật thông tin cá nhân | User / Admin |
| `GET/POST` | `/api/v1/users/me/addresses` | Danh sách/thêm địa chỉ | User / Admin |
| `PUT/DELETE` | `/api/v1/users/me/addresses/{id}` | Cập nhật/xóa địa chỉ | User / Admin |

---

## 🐰 4. Event Publisher (RabbitMQ)

Class `AuthEventPublisher.java` phát sự kiện OTP gửi mail:

```java
public void sendOtpEvent(String email, String otpCode, String otpType) {
    OtpEmailEvent event = new OtpEmailEvent(
        email, 
        otpCode, 
        otpType, 
        LocalDateTime.now()
    );
    rabbitTemplate.convertAndSend("auth.exchange", "otp.send", event);
}
```
