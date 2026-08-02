# 🔍 Troubleshooting Guide

> Hướng dẫn xử lý sự cố và các lỗi thường gặp trong quá trình chạy và phát triển **Kyro Backend**.

---

## 🛑 1. Lỗi Xung Đột Cổng (Port Conflict)

### Triệu chứng:
`Error starting userland proxy: listen tcp4 0.0.0.0:8080: bind: address already in use`

### Hướng xử lý:
Kiểm tra và tắt tiến trình đang chiếm cổng trên máy local:
- **macOS / Linux**:
  ```bash
  sudo lsof -i :8080
  kill -9 <PID>
  ```
- **Windows (PowerShell)**:
  ```powershell
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F
  ```

---

## 🛑 2. Lỗi Spotless Code Formatting Khi Build (CI/CD / Maven)

### Triệu chứng:
`[ERROR] Failed to execute goal com.diffplug.spotless:spotless-maven-plugin: ... The following files had format violations`

### Hướng xử lý:
Chạy lệnh tự động căn chỉnh code theo Google Java Format:
```bash
task format
# Hoặc chạy: ./mvnw spotless:apply
```

---

## 🛑 3. Lỗi Kết Nối Database PostgreSQL / Flyway Migration Failure

### Triệu chứng:
`FlywayException: Validate failed: Migrations have failed validation` hoặc không thể tạo database.

### Hướng xử lý:
Nếu đang ở môi trường phát triển cục bộ và muốn reset lại trạng thái database ban đầu:
```bash
task clean
task infra
```

---

## 🛑 4. Lỗi RabbitMQ Connection Refused Khi Khởi Động Service

### Triệu chứng:
`com.rabbitmq.client.AuthenticationFailureException` hoặc `java.net.ConnectException: Connection refused`.

### Hướng xử lý:
1. Đảm bảo container RabbitMQ đã khởi động hoàn tất (`task db:up`).
2. Kiểm tra giao diện RabbitMQ Management tại `http://localhost:15672` (User: `guest`, Password: `guest`).

---

## 🛑 5. Lỗi Maven Wrapper Export Module Java 21 JDK Compiler

### Triệu chứng:
`java.lang.IllegalAccessError: class com.diffplug.spotless.extra.java.GoogleJavaFormatStep`

### Hướng xử lý:
Lệnh `task format` đã tích hợp sẵn flag `MAVEN_OPTS` mở rộng export module compiler cho Java 21:
```bash
task format
```
