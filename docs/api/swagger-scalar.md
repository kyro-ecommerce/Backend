# OpenAPI và Scalar

## Truy cập đúng

Sau khi stack healthy, mở Scalar aggregator tại:

```text
http://localhost:8080/scalar
```

Gateway không tạo một file `/v3/api-docs` gộp mọi API. Scalar cấu hình nhiều source riêng và Gateway proxy từng OpenAPI JSON:

| Source | URL |
| --- | --- |
| Auth | `http://localhost:8080/auth-service/v3/api-docs` |
| Catalog | `http://localhost:8080/catalog-service/v3/api-docs` |
| Cart | `http://localhost:8080/cart-service/v3/api-docs` |
| Order | `http://localhost:8080/order-service/v3/api-docs` |
| Payment | `http://localhost:8080/payment-service/v3/api-docs` |
| Notification | `http://localhost:8080/notification-service/v3/api-docs` |

AI là ứng dụng ngoài repository. Nếu chạy ở `localhost:8000` và có FastAPI docs, đường dẫn thường là `/docs` và `/openapi.json`, nhưng phải kiểm tra repo/config AI thay vì xem đó là đảm bảo của backend Java.

## Thử endpoint

1. Login qua `POST /api/v1/auth/login` để lấy access token.
2. Với endpoint bảo vệ, gửi `Authorization: Bearer <accessToken>`.
3. Không tự gửi `X-User-Id`, `X-User-Email`, `X-User-Roles`; Gateway tạo chúng từ JWT.
4. Gọi qua port `8080`, không gọi trực tiếp business service.
5. Internal endpoints cần `X-Internal-Token` và chỉ dành cho service-to-service, không dùng để demo client API.

Nếu Scalar lên nhưng một source lỗi, kiểm tra service tương ứng trong Eureka/health/log. Nếu toàn bộ source lỗi, kiểm tra Config Server, Eureka và route docs ở `config-server/src/main/resources/config/api-gateway.yml`.
