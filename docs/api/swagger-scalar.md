# 📖 Swagger & Scalar OpenAPI Documentation

> Hướng dẫn truy cập tài liệu API trực quan **Swagger UI** và **Scalar OpenAPI Docs** tự động tích hợp trong hệ thống Kyro Microservices.

---

## 🌐 1. Truy Cập Giao Diện OpenAPI Docs

Tất cả các Microservices đều tích hợp `springdoc-openapi-starter-webmvc-ui`. Khi hệ thống đang khởi chạy qua API Gateway (`task infra` hoặc `task run`), bạn có thể truy cập các đường dẫn sau:

### 1.1. Aggregated API Gateway OpenAPI Docs
- **URL OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **Tập hợp**: Hiển thị tài liệu REST API tổng hợp của tất cả các microservices (`auth-service`, `catalog-service`, `cart-service`, `order-service`, `payment-service`) qua một cổng duy nhất `:8080`.

### 1.2. Python AI Service Swagger UI
- **URL Interactive UI**: `http://localhost:8000/docs`
- **URL ReDoc UI**: `http://localhost:8000/redoc`
- **Tính năng**: Thử nghiệm trực tiếp các endpoint gợi ý sản phẩm (`/api/v1/ai/recommend`) và tìm kiếm ngôn ngữ tự nhiên (`/api/v1/ai/semantic-search`).

---

## 🛠️ 2. Thử Nghiệm API Trực Tiếp Trên Swagger UI

1. Đảm bảo ứng dụng đang khởi chạy.
2. Truy cập `http://localhost:8080/v3/api-docs`.
3. Nhấn nút **Authorize** ở góc phải giao diện Swagger UI.
4. Nhập chuỗi Bearer JWT Token thu được từ API `/api/v1/auth/login`.
5. Nhấn **Try it out** để thực thi các yêu cầu REST API trực tiếp.
