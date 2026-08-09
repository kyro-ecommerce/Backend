# 📊 Bộ Kịch Bản Test Hiệu Năng k6 Cho Hệ Thống Kyro Microservices

Thư mục này chứa bộ kịch bản kiểm thử tải (**Performance & Load Testing**) hoàn chỉnh, chuyên sâu và cặn kẽ dành cho hệ thống Kyro E-Commerce Microservices Platform.

---

## 🏗️ Cấu Trúc Bộ Test (`k6/`)

```text
k6/
├── README.md                     # 📖 Tài liệu hướng dẫn sử dụng & phân tích chỉ số
├── main_suite.js                 # 🚀 Master Suite (Chạy đồng thời toàn bộ user journeys với tỷ lệ thực tế)
├── config/
│   └── environments.js           # ⚙️ Cấu hình Target Endpoint, Auth Headers & SLAs Thresholds
├── data/
│   └── test-data.js              # 🎲 Generator dữ liệu test, Mock users, filters & edge case payloads
├── utils/
│   ├── auth-helper.js            # 🔐 Tự động đăng nhập, lấy JWT token, tạo địa chỉ giao hàng
│   └── metrics.js                # 📈 Định nghĩa Custom Trends, Counters, Success Rates từng service
└── scenarios/
    ├── 01_smoke_test.js          # 🩺 Smoke Test (Xác nhận hệ thống sẵn sàng trong 30s)
    ├── 02_browse_catalog.js      # 🏷️ Read-Heavy Load Test (Duyệt sản phẩm, lọc, chi tiết, đánh giá & AI Search)
    ├── 03_cart_operations.js     # 🛒 High-Concurrency Redis Cart Test (Thêm, xem, sửa, xóa giỏ hàng)
    ├── 04_checkout_order.js      # 📦 Write-Heavy Transaction Test (Auth -> Cart -> Order -> VNPay -> Callback)
    ├── 05_stress_test.js         # 💥 System Stress Test (Tăng tải đến 300 VUs tìm ngưỡng chịu tải tối đa)
    ├── 06_spike_test.js          # ⚡ Flash Sale Spike Test (Bột phát 0 -> 250 VUs trong 10s)
    └── 07_edge_cases.js          # 🧪 Edge Cases & Resilience Chaos Test (Sai token, payload lỗi, out-of-bound ID, rate-limit)
```

---

## ⚙️ 1. Yêu Cầu Tiền Đề (Prerequisites)

1. **Cài đặt k6**:
   - **macOS**: `brew install k6`
   - **Windows**: `winget install k6` hoặc `choco install k6`
   - **Linux**: `sudo apt-get install k6`
2. **Khởi động Backend Kyro**:
   - Đảm bảo các service đang chạy (Gateway `:8080`, Postgres, Redis, RabbitMQ, v.v.).
   - Khởi động qua Docker: `task run` hoặc `task infra`.

---

## 🚀 2. Các Lệnh Chạy Test (`k6 run` / `task`)

Bạn có thể sử dụng công cụ `task` (Taskfile.yml) hoặc lệnh `k6 run` trực tiếp:

Các scenario có đăng nhập bắt buộc dùng một tài khoản chuyên dụng có role `CUSTOMER`. Không lưu credential trong source:

```bash
cp .env.example .env
# Điền K6_USER_EMAIL và K6_USER_PASSWORD trong .env local (đã được gitignore),
# hoặc inject hai biến này từ secret store của CI.
```

Nếu gọi `k6` trực tiếp mà không qua `task`, export hai biến trước khi chạy. Suite sẽ dừng ngay ở init phase nếu thiếu credential.

### 🩺 a. Smoke Test (Kiểm tra sức khỏe nhanh)
```bash
task k6:smoke
# Hoặc: k6 run k6/scenarios/01_smoke_test.js
```

### 🏷️ b. Catalog & Search Load Test (Tải duyệt sản phẩm)
```bash
task k6:browse
# Hoặc: k6 run k6/scenarios/02_browse_catalog.js
```

### 🛒 c. Redis Cart Concurrency Test (Tải thao tác giỏ hàng)
```bash
task k6:cart
# Hoặc: k6 run k6/scenarios/03_cart_operations.js
```

### 📦 d. Full Checkout & Payment Flow (Tải đặt hàng & VNPay)
```bash
task k6:checkout
# Hoặc: k6 run k6/scenarios/04_checkout_order.js
```

### 💥 e. System Stress Test (Tăng dần lên 300 VUs)
```bash
task k6:stress
# Hoặc: k6 run k6/scenarios/05_stress_test.js
```

### ⚡ f. Flash Sale Spike Test (Đột biến traffic 250 VUs)
```bash
task k6:spike
# Hoặc: k6 run k6/scenarios/06_spike_test.js
```

### 🧪 g. Edge Cases & Resilience Test (Test case biên & lỗi)
```bash
task k6:edge
# Hoặc: k6 run k6/scenarios/07_edge_cases.js
```

### 🌟 h. Master Suite (Chạy toàn bộ kịch bản mô phỏng thực tế)
```bash
task k6:full
# Hoặc: k6 run k6/main_suite.js
```

---

## 🧪 3. Chi Tiết Các Edge Cases Được Kiểm Thử (`07_edge_cases.js`)

Bộ test này thực hiện test cặn kẽ các tình huống biên & lỗi tiềm ẩn trong microservices:

1. **JWT & Gateway Authentication Filter**:
   - Gửi request chứa Token rác (`Bearer invalid_token_123`).
   - Gửi request chứa Chữ ký JWT sai (`eyJhbGciOi...`).
   - Gửi Token trống hoặc sai định dạng.
   - **Kỳ vọng**: API Gateway trả về `401 Unauthorized` hoặc `403 Forbidden` ngay tại cửa ngõ.

2. **Xác Thực Đăng Nhập & Mật Khẩu Sai**:
   - Đăng nhập với email không tồn tại hoặc mật khẩu sai.
   - **Kỳ vọng**: Auth service trả về `401 Bad Credentials` không bị nghẽn DB.

3. **Tài Nguyên Không Tồn Tại (404 Not Found)**:
   - Truy vấn Product ID cực lớn (`9999999`), ID âm (`-1`), ID bằng `0`.
   - **Kỳ vọng**: Catalog service phản hồi `404 Not Found` không văng `NullPointerException` (500).

4. **Số Lượng Giỏ Hàng Bất Hợp Lệ**:
   - Cập nhật số lượng giỏ hàng âm (`quantity: -5`) hoặc số lượng khổng lồ (`999999`).
   - **Kỳ vọng**: Cart Service kiểm tra validate DTO và phản hồi `400 Bad Request`.

5. **Callback Webhook VNPay Bị Biến Dạng**:
   - Gọi callback VNPay `/vnpay-callback` thiếu tham số `vnp_TxnRef`.
   - **Kỳ vọng**: Payment Service trả về lỗi `400 Bad Request` an toàn.

6. **Rate Limiting Gửi OTP (`429 Too Many Requests`)**:
   - Yêu cầu gửi lại mã OTP 2 lần liên tiếp ngay lập tức.
   - **Kỳ vọng**: Auth Service chặn bằng `429 Too Many Requests` (hoặc `400`) theo cơ chế cooldown.

---

## 📈 4. Chỉ Số Đo Lường & Ngưỡng SLA (Metrics & Thresholds)

Mỗi kịch bản test sẽ tự động thu thập các thông số chuẩn SLA:

| Indicator / Metric | Ý Nghĩa Chuyên Môn | Ngưỡng Kỳ Vọng (SLA) |
| :--- | :--- | :---: |
| **`http_req_duration (p95)`** | 95% số request có thời gian phản hồi dưới mức này | `< 500 ms` |
| **`http_req_duration (p99)`** | 99% số request có thời gian phản hồi dưới mức này | `< 1500 ms` |
| **`http_req_failed`** | Tỷ lệ request bị lỗi HTTP (4xx / 5xx) | `< 1%` |
| **`kyro_auth_req_duration`** | Thời gian giải mã JWT & Đăng nhập tại Auth Service | `< 400 ms` |
| **`kyro_cart_req_duration`** | Thời gian thao tác giỏ hàng In-Memory trên Redis | `< 200 ms` |
| **`kyro_order_req_duration`** | Thời gian xử lý transaction Checkout trên PostgreSQL | `< 800 ms` |

---

## 🛠️ 5. Gợi Ý Tối Ưu Hiệu Năng Hệ Thống Kyro Khi Chạy Test

Nếu phát hiện hiện tượng nghẽn (**bottleneck**) trong quá trình stress/spike test, hãy chú ý các điểm cấu hình sau:

1. **HikariCP Database Connection Pool (`application.yml`)**:
   - Mặc định Spring Boot để `maximum-pool-size: 10`. Khi test 100+ VUs checkout, DB pool sẽ bị exhausted.
   - Khuyên dùng: Tăng `maximum-pool-size: 30` cho `order-service` và `catalog-service`.

2. **Redis Connection Pool (`cart-service`)**:
   - Cấu hình Lettice / Jedis connection pool hỗ trợ tối đa 50-100 concurrent connections cho giỏ hàng siêu tốc.

3. **Virtual Threads (Java 21 LTS)**:
   - Thêm cấu hình `spring.threads.virtual.enabled=true` trong Spring Boot 3.3 cho tất cả microservices để xử lý hàng ngàn I/O requests song song mà không tốn Thread pool OS.

4. **API Gateway Netty Event Loop**:
   - Spring Cloud Gateway chạy trên Netty Reactor. Đảm bảo không sử dụng bất kỳ blocking call (như JDBC trực tiếp hay synchronous feign) trong Gateway Filter.
