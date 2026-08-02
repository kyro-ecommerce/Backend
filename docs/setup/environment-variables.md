# ⚙️ Environment Variables Reference

> Danh sách chi tiết toàn bộ các biến môi trường trong file `.env` sử dụng cho Docker Compose và các Microservices.

---

## 📋 Bảng Chi Tiết Biến Môi Trường

| Tên Biến Môi Trường | Giá Trị Mặc Định / Example | Mô Tả & Mục Đích Sử Dụng |
| :--- | :--- | :--- |
| `COMPOSE_PROJECT_NAME` | `kyro-backend` | Tên dự án Docker Compose |
| `PORT` | `8080` | Cổng HTTP lắng nghe chính của API Gateway |
| `API_PREFIX` | `/api/v1` | Prefix tiền tố cho tất cả các REST APIs |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/postgres` | Đường dẫn kết nối JDBC PostgreSQL |
| `DATABASE_USERNAME` | `postgres` | Tài khoản quản trị CSDL PostgreSQL |
| `DATABASE_PASSWORD` | `postgres_password` | Mật khẩu kết nối CSDL PostgreSQL |
| `DATABASE_DB` | `postgres` | Tên database mặc định khi khởi tạo postgres container |
| `JPA_DDL_AUTO` | `update` | Cấu hình Hibernate DDL (`update`, `validate`, `none`) |
| `JPA_SHOW_SQL` | `true` | Bật/Tắt in câu lệnh SQL ra Console |
| `JWT_SECRET` | `404E6352...` | Chuỗi bí mật 256-bit mã hóa chữ ký JWT Tokens |
| `JWT_ACCESS_EXPIRATION_MS` | `3600000` | Thời hạn sống của Access Token (1 giờ = 3,600,000ms) |
| `JWT_REFRESH_EXPIRATION_MS` | `86400000` | Thời hạn sống của Refresh Token (24 giờ = 86,400,000ms) |
| `OAUTH2_GOOGLE_CLIENT_ID` | `mock-google-client-id` | Client ID Google OAuth2 Console |
| `OAUTH2_GOOGLE_CLIENT_SECRET` | `mock-google-client-secret` | Client Secret Google OAuth2 Console |
| `OAUTH2_GITHUB_CLIENT_ID` | `mock-github-client-id` | Client ID GitHub Developer Settings |
| `OAUTH2_GITHUB_CLIENT_SECRET` | `mock-github-client-secret` | Client Secret GitHub Developer Settings |
| `MAIL_HOST` | `smtp.gmail.com` | Địa chỉ máy chủ SMTP sending email |
| `MAIL_PORT` | `587` | Cổng gửi thư SMTP (TLS) |
| `MAIL_USERNAME` | `your_email@gmail.com` | Tài khoản email gửi thư hệ thống |
| `MAIL_PASSWORD` | `your_app_password` | Mật khẩu ứng dụng (App Password Google) |
| `CLOUDINARY_CLOUD_NAME` | `kyro_cloud` | Tên Cloudinary Cloud lưu trữ ảnh sản phẩm |
| `CLOUDINARY_API_KEY` | `123456789` | API Key truy cập Cloudinary |
| `CLOUDINARY_API_SECRET` | `secret_key` | API Secret truy cập Cloudinary |
| `VNPAY_TMN_CODE` | `VNPAY_MERCHANT_CODE` | Mã Merchant do VNPay cung cấp |
| `VNPAY_HASH_SECRET` | `VNPAY_HASH_SECRET` | Chuỗi Secret mã hóa SHA-512 giao dịch VNPay |
| `VNPAY_PAY_URL` | `https://sandbox.vnpayment.vn/...` | Đường dẫn kết nối Sandbox VNPay Payment |
| `VNPAY_RETURN_URL` | `http://localhost:8080/api/v1/...` | Webhook Callback nhận kết quả thanh toán VNPay |
| `GEMINI_API_KEY` | `AIzaSy...` | API Key Google Gemini sinh Vector Embeddings |
