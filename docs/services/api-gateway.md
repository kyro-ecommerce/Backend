# ⚡ API Gateway Service Documentation

> **Service Name**: `api-gateway`  
> **Port**: `8080`  
> **Framework**: Spring Cloud Gateway (Spring Boot 3.3.2, Java 21)  
> **Package**: `com.kyro.gateway`

---

## 📌 1. Chức Năng Chính

**API Gateway** đóng vai trò là điểm truy cập duy nhất (Reverse Proxy & Security Offloader) cho toàn bộ hệ thống microservices. Gateway thực hiện:

1. **Bảo Mật & Lọc Token Tập Trung**:
   - Kiểm tra JWT Token tại `AuthenticationFilter.java`.
   - Giải mã và xác thực chữ ký JWT với `JWT_SECRET`.
   - Bỏ qua kiểm tra JWT đối với các public endpoints (Login, Register, Swagger, Refresh Token).
2. **Inject Header Định Danh (Header Enrichment)**:
   - Trích xuất thông tin người dùng từ JWT claims và thêm các HTTP headers trước khi chuyển tiếp cho downstream service:
     - `X-User-Id`: ID người dùng (ví dụ: `102`)
     - `X-User-Email`: Email người dùng (ví dụ: `user@kyro.com`)
     - `X-User-Roles`: Danh sách quyền (ví dụ: `ROLE_USER,ROLE_ADMIN`)
3. **Cấu Hình CORS Tập Trung**:
   - Cho phép các frontend client (`http://localhost:3000`, `http://localhost:5173`) truy cập với đầy đủ HTTP Methods (GET, POST, PUT, DELETE, OPTIONS).
4. **Định Tuyến Động (Dynamic Routing)**:
   - Tích hợp **Eureka Discovery Client** định tuyến yêu cầu dựa trên tên dịch vụ (`lb://AUTH-SERVICE`, `lb://CATALOG-SERVICE`, v.v.).

---

## 🛣️ 2. Bảng Định Tuyến Routes (Routing Configuration)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/v1/auth/**, /api/v1/users/**, /api/v1/admin/users/**

        - id: catalog-service
          uri: lb://CATALOG-SERVICE
          predicates:
            - Path=/api/v1/products/**, /api/v1/categories/**, /api/v1/reviews/**

        - id: cart-service
          uri: lb://CART-SERVICE
          predicates:
            - Path=/api/v1/carts/**

        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/v1/orders/**, /api/v1/admin/orders/**

        - id: payment-service
          uri: lb://PAYMENT-SERVICE
          predicates:
            - Path=/api/v1/payments/**

        - id: ai-service
          uri: http://kyro-ai-service:8000
          predicates:
            - Path=/api/v1/ai/**
```

---

## 🔐 3. Triển Khai AuthenticationFilter

Class `AuthenticationFilter.java` kế thừa `AbstractGatewayFilterFactory`:

```java
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouterValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing authorization header");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(authHeader);
                    Claims claims = jwtUtil.getAllClaimsFromToken(authHeader);
                    
                    // Header Injection
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", claims.getSubject())
                            .header("X-User-Email", claims.get("email", String.class))
                            .header("X-User-Roles", claims.get("roles", String.class))
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access");
                }
            }
            return chain.filter(exchange);
        };
    }
}
```

---

## 🧪 4. Endpoint Kiểm Tra Actuator & Health Check

- **Health Check URL**: `http://localhost:8080/actuator/health`
- **Swagger Open API Direct Proxy**: `http://localhost:8080/v3/api-docs`
