# 🚀 Local Development Guide

> Hướng dẫn thiết lập môi trường phát triển cục bộ (Local Development) cho dự án **Kyro Backend** với tốc độ **Hot-Reloading chỉ 1 - 2 giây**.

---

## 📌 1. Yêu Cầu Cài Đặt (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

1. **Docker & Docker Compose (V2+)**:
   - macOS / Windows: Cài đặt [Docker Desktop](https://www.docker.com/products/docker-desktop/).
2. **Java 21 LTS**:
   - Tải Java 21 OpenJDK hoặc temurin-21.
   - Bạn cũng có thể dùng Maven wrapper `./mvnw` có sẵn trong repo mà không cần cài Maven rời.
3. **Go Task (`task`)**:
   - Công cụ shortcut lệnh (Khuyên dùng):
     - macOS: `brew install go-task`
     - Windows: `scoop install task` hoặc `choco install go-task`

---

## ⚡ 2. Quy Trình Phát Triển Nhanh Cho Lập Trình Viên (Hot-Reload Mode)

> [!TIP]
> **Đừng chạy `docker compose up` toàn bộ mọi lúc khi đang sửa code!**  
> Việc rebuild lại file JAR và Docker images sẽ tốn từ 2 - 3 phút. Hãy sử dụng quy trình **Hot-Reloading** bên dưới để sửa code và xem kết quả sau 1 giây:

### ⚡ Bước 1: Khởi động Hạ tầng ngầm & API Gateway (Chạy 1 lần)
Mở Terminal tại thư mục root backend và gõ:
```bash
task infra
```
Lệnh này sẽ khởi động ngầm các container:
- PostgreSQL (Port `5432`)
- Redis (Port `6379`)
- RabbitMQ (Port `5672`, Dashboard `:15672`)
- Eureka Server (Port `8761`)
- Config Server (Port `8888`)
- API Gateway (Port `8080`)

### ⚡ Bước 2: Chạy Service bạn đang sửa code ở chế độ Local Hot-Reload
Ví dụ bạn đang viết tính năng mới cho `auth-service` hoặc `catalog-service`:

```bash
# Code & Hot-Reload tức thì cho Auth Service
task dev:auth

# Hoặc cho Catalog Service
task dev:catalog

# Hoặc cho Order Service
task dev:order
```

🔥 **Lợi ích**: Nhờ tích hợp **Spring Boot DevTools**, mỗi khi bạn sửa file `.java` và nhấn **Save (`Cmd+S` / `Ctrl+S`)**, ứng dụng Java sẽ **tự động nạp lại mã nguồn sau 1-2 giây** mà KHÔNG cần build lại Docker image!

---

## 🐳 3. Danh Sách Lệnh Shortcut Shortcuts (`Taskfile.yml`)

| Lệnh `task` | Mô Tả | Thời Gian |
| :--- | :--- | :---: |
| `task infra` | **[KHUYÊN DÙNG]** Khởi chạy toàn bộ hạ tầng (Postgres, Redis, RabbitMQ, Gateway, Eureka, Config). | ⚡ Instant |
| `task dev:auth` | **[HOT-RELOAD]** Chạy `auth-service` ở máy local kèm phản hồi 1s. | ⚡ 1-2s |
| `task dev:catalog` | **[HOT-RELOAD]** Chạy `catalog-service` ở máy local. | ⚡ 1-2s |
| `task dev:cart` | **[HOT-RELOAD]** Chạy `cart-service` ở máy local. | ⚡ 1-2s |
| `task dev:order` | **[HOT-RELOAD]** Chạy `order-service` ở máy local. | ⚡ 1-2s |
| `task dev:payment` | **[HOT-RELOAD]** Chạy `payment-service` ở máy local. | ⚡ 1-2s |
| `task dev:notification` | **[HOT-RELOAD]** Chạy `notification-service` ở máy local. | ⚡ 1-2s |
| `task run` | Build và chạy TOÀN BỘ 11 container trong Docker Compose. | 🐢 1-3 min |
| `task stop` | Dừng tất cả container Docker đang chạy. | ⚡ Instant |
| `task clean` | Dừng và **XÓA SẠCH** Volume dữ liệu Postgres / Redis. | ⚡ Instant |
| `task format` | Tự động định dạng mã nguồn theo Google Java Format. | ⚡ Instant |
| `task format:check` | Kiểm tra định dạng code (Dùng cho CI/CD). | ⚡ Instant |
| `task test` | Chạy toàn bộ Unit Tests trong dự án. | ⚡ Instant |
| `task logs:ui` | Mở giao diện xem log Dozzle trên trình duyệt (`http://localhost:9999`). | ⚡ Instant |
