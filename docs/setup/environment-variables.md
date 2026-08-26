# Biến môi trường

Sao chép `.env.example` thành `.env`, thay toàn bộ giá trị `mock_*`/`replace-*`, và không commit `.env`.

## Bắt buộc để Compose khởi động đầy đủ

| Nhóm | Biến | Ý nghĩa |
| --- | --- | --- |
| Database | `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `DATABASE_DB` | Tài khoản/container PostgreSQL; mỗi service override JDBC URL sang database riêng |
| Internal auth | `INTERNAL_API_TOKEN` | Shared secret gắn vào mọi Feign request nội bộ; phải dài, ngẫu nhiên và giống nhau giữa service |
| JWT | `JWT_SECRET` | Shared HMAC secret được Compose cấp cho Auth và Gateway; đổi secret làm token cũ vô hiệu |
| SMTP | `MAIL_USERNAME`, `MAIL_PASSWORD` | Notification gửi OTP/order mail; Compose yêu cầu có giá trị |
| Logo | `COMPANY_LOGO_URL` | URL logo trong email; Compose yêu cầu có giá trị |
| Cloudinary | `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Catalog upload/xóa image; Compose yêu cầu có giá trị |
| VNPay | `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, `VNPAY_PAY_URL`, `VNPAY_RETURN_URL` | Merchant/sandbox signing và redirect |

OAuth2 có giá trị mock mặc định nên service có thể boot nhưng login Google/GitHub không hoạt động đến khi đặt credentials thật. Performance script dùng fixture user và tự ký JWT bằng `JWT_SECRET`, không cần tài khoản customer thật.

## Các giá trị cần hiểu khi bảo vệ

- `JWT_ACCESS_EXPIRATION_MS=3600000`: access token 1 giờ.
- `JWT_REFRESH_EXPIRATION_MS=86400000`: refresh token 1 ngày.
- `OTP_EXPIRATION_MINUTES=10`, `OTP_RESEND_COOLDOWN_MINUTES=1`.
- `USE_SECURE_COOKIE=false` phù hợp HTTP local; production HTTPS phải bật và rà soát SameSite/domain.
- `VNPAY_RETURN_URL` hiện mặc định là frontend. Backend callback thật là `/api/v1/payment-providers/vnpay/callback`; frontend/integration phải chuyển đúng params tới endpoint này.
- `AI_SERVICE_URI` được Compose cấp trực tiếp cho Gateway, mặc định `http://host.docker.internal:8000`; AI service không được build hoặc benchmark trong repository này.
- Redis/Rabbit/Eureka/Config host được Compose override bằng tên container, không dùng localhost bên trong container.

## Quy tắc production

- Dùng secret manager, không dùng default trong YAML.
- Tách token/credential theo môi trường và rotate định kỳ.
- Không log secret, VNPay secure hash, JWT, OTP hoặc mail password.
- Chỉ expose Gateway; giới hạn management port của PostgreSQL/Redis/RabbitMQ/Eureka/Config/Dozzle.
- Bật TLS và secure cookie; đặt CORS đúng domain thật.
