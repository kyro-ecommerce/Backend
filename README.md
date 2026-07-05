# Kyro Microservices Backend

Hệ thống Backend Microservices cho ứng dụng E-commerce Kyro được xây dựng trên nền tảng **Spring Boot**, **Spring Cloud** và điều phối container bằng **Docker Compose**.

---

## ⚡ Hướng Dẫn Chạy Dự Án (Quick Start)

Dự án sử dụng **Docker Compose** và **Taskfile** (`task`) để tối giản hóa việc quản lý và vận hành toàn bộ hệ thống dịch vụ.

### 📌 Yêu Cầu Hệ Thống (Prerequisites)
* **Docker** & **Docker Compose** đã được cài đặt và đang chạy.
* **Go Task** (Khuyến khích) để chạy các shortcut. Cài đặt nhanh:
  * MacOS: `brew install go-task`
  * Windows (via Scoop): `scoop install task`
  * Hoặc bạn có thể chạy trực tiếp các lệnh docker/maven tương ứng.

### 🚀 Khởi Chạy Dự Án

#### Cách 1: Sử dụng Taskfile (Khuyên Dùng)
1. **Build và Khởi Chạy lần đầu tiên:**
   ```bash
   task dev
   ```
   *Lệnh này sẽ biên dịch mã nguồn Java, đóng gói file JAR và build các Docker Image cho 9 microservice, sau đó khởi chạy tất cả container.*

2. **Chỉ khởi chạy lại các container đã build:**
   ```bash
   task run
   ```

3. **Dừng toàn bộ hệ thống:**
   ```bash
   task stop
   ```

4. **Xóa toàn bộ container và dữ liệu DB (nếu cần reset hoàn toàn):**
   ```bash
   task clean
   ```

#### Cách 2: Sử dụng Docker Compose Thủ Công
Nếu chưa cài đặt `task`, bạn có thể thực hiện theo các bước sau:
1. **Biên dịch và đóng gói JAR cho dự án:**
   ```bash
   ./mvnw clean package -DskipTests
   ```
2. **Khởi chạy container và build image:**
   ```bash
   docker compose up --build -d
   ```
3. **Dừng container:**
   ```bash
   docker compose down
   ```

---

## 🎨 Chuẩn Hóa Định Dạng Mã Nguồn (Formatting Standard)

Để đảm bảo toàn bộ mã nguồn tuân thủ một chuẩn chung, dự án tích hợp **Spotless** và áp dụng **Google Java Format (style GOOGLE)**.

Hệ thống tự động loại bỏ các import không sử dụng và căn chỉnh thụt dòng theo đúng chuẩn Google Java Format. Bạn có thể sử dụng các lệnh sau để tự động định dạng mã nguồn:

* **Tự động định dạng toàn bộ mã nguồn Java:**
  ```bash
  task format
  ```
  *(Hoặc chạy lệnh Maven: `./mvnw spotless:apply`)*

* **Kiểm tra xem mã nguồn có đúng định dạng hay chưa (dùng cho CI/CD):**
  ```bash
  task format:check
  ```
  *(Hoặc chạy lệnh Maven: `./mvnw spotless:check`)*

---

## 🛠️ Cấu Trúc Hệ Thống & Microservices

Hệ thống bao gồm **9 Microservices** độc lập phối hợp với nhau thông qua mạng nội bộ Docker:

| Tên Dịch Vụ | Cổng (Port) | Vai Trò & Nhiệm Vụ |
| :--- | :---: | :--- |
| **eureka-server** | `8761` | Service Registry - Đăng ký và phát hiện dịch vụ. |
| **config-server** | `8888` | Config Server - Quản lý cấu hình tập trung cho các service. |
| **api-gateway** | `8080` | API Gateway - Định tuyến request và lọc JWT Authentication. |
| **auth-service** | `8081` | Quản lý người dùng, phân quyền, xác thực OAuth2 & OTP. |
| **catalog-service** | `8082` | Quản lý danh mục sản phẩm, bộ lọc tìm kiếm và tải ảnh Cloudinary. |
| **cart-service** | `8083` | Quản lý giỏ hàng tạm thời và lưu trữ Redis. |
| **order-service** | `8084` | Quản lý quy trình đặt hàng, xử lý trạng thái đơn hàng. |
| **payment-service** | `8085` | Tích hợp cổng thanh toán VNPay. |
| **notification-service**| *Internal* | Lắng nghe hàng đợi RabbitMQ để gửi email xác thực/đơn hàng. |

### 🗄️ Cơ Sở Dữ Liệu & Hạ Tầng
* **PostgreSQL**: Chạy ở cổng `5432` chứa 4 cơ sở dữ liệu độc lập:
  * `postgres` (dành cho `auth-service`)
  * `kyro_catalog` (dành cho `catalog-service`)
  * `kyro_order` (dành cho `order-service`)
  * `kyro_payment` (dành cho `payment-service`)
* **Flyway Migration**: Mỗi microservice tự quản lý schema của mình thông qua các script SQL migration đặt tại `src/main/resources/db/migration/`.
* **Redis**: Chạy ở cổng `6379` để cache và lưu trữ thông tin giỏ hàng (`cart-service`).
* **RabbitMQ**: Chạy ở cổng `5672` (Web UI quản trị: `15672`) xử lý tin nhắn bất đồng bộ gửi email thông báo.

---

## 🐳 Danh Sách Các Lệnh Taskfile Tiện Ích

Dưới đây là bảng tổng hợp các lệnh định nghĩa sẵn trong [Taskfile.yml](file:///Users/tphuc263/Project/Kyro/backend/Taskfile.yml):

| Lệnh `task` | Mô tả chi tiết |
| :--- | :--- |
| `task dev` | Biên dịch Java và khởi chạy toàn bộ dịch vụ (build mới Docker images). |
| `task run` | Bật các container đã tồn tại của toàn bộ hệ thống. |
| `task stop` | Dừng và tắt toàn bộ container của hệ thống. |
| `task clean` | Dừng hệ thống và xóa sạch volume dữ liệu DB. |
| `task format` | Tự động định dạng toàn bộ mã nguồn theo chuẩn Google Java Format. |
| `task format:check` | Kiểm tra định dạng mã nguồn. |
| `task db:up` | Chỉ khởi động các container hạ tầng (Postgres, Redis, RabbitMQ). |
| `task db:down` | Dừng các container hạ tầng. |
| `task db:logs` | Xem log trực tiếp của các container hạ tầng. |
