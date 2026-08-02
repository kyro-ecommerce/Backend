# 🗄️ Database Design & Storage Schema

> **Kyro Backend** tuân thủ nghiêm ngặt nguyên tắc **Database-per-service**. Mỗi Microservice có toàn quyền sở hữu cơ sở dữ liệu riêng, đảm bảo không có truy vấn SQL liên dịch vụ (Cross-database JOINs).

---

## 📊 1. Bảng Tổng Quan Database Per Service

| Microservice | Engine | Schema / DB Name | Tool Migration | Mục Đích Lưu Trữ |
| :--- | :--- | :--- | :--- | :--- |
| **Auth Service** | PostgreSQL 16 | `kyro_auth` | Flyway | Tài khoản người dùng, Roles, Addresses, OAuth2 credentials, Refresh tokens |
| **Catalog Service** | PostgreSQL 16 | `kyro_catalog` | Flyway | Danh mục sản phẩm, biến thể (Size/Stock), đánh giá (Reviews), Ảnh Cloudinary |
| **Order Service** | PostgreSQL 16 | `kyro_order` | Flyway | Đơn hàng, chi tiết đơn hàng, địa chỉ giao hàng snapshot, trạng thái thanh toán |
| **Payment Service** | PostgreSQL 16 | `kyro_payment` | Flyway | Lịch sử giao dịch VNPay, IPN Callbacks, Mã giao dịch ngân hàng |
| **Cart Service** | Redis 7 | Key-Value Storage | *N/A* | Giỏ hàng tạm thời với TTL (Expire sau 30 ngày) |
| **AI Service** | PostgreSQL 16 + `pgvector` | `postgres` (`public`) | Alembic (Python) | Product Vector Embeddings (768 chiều), Product Catalog Sync Index |

---

## 🏛️ 2. Chi Tiết Schema & Entities Từng Service

### 2.1. Auth Service Database (`kyro_auth`)
Cơ sở dữ liệu lưu thông tin người dùng và phân quyền RBAC:
- **`users`**: `id` (PK), `email` (Unique), `password` (BCrypt), `full_name`, `phone`, `role` (`ROLE_USER`, `ROLE_ADMIN`), `provider` (`LOCAL`, `GOOGLE`, `GITHUB`), `enabled`, `created_at`, `updated_at`.
- **`addresses`**: `id` (PK), `user_id` (FK), `recipient_name`, `phone_number`, `street_address`, `ward`, `district`, `city`, `is_default`.
- **`refresh_tokens`**: `id` (PK), `user_id` (FK), `token` (Unique), `expiry_date`, `revoked`.

### 2.2. Catalog Service Database (`kyro_catalog`)
Cơ sở dữ liệu danh mục sản phẩm và kho hàng:
- **`categories`**: `id` (PK), `name`, `slug` (Unique), `parent_id` (Self-FK cho danh mục đa cấp), `image_url`.
- **`products`**: `id` (PK), `name`, `slug` (Unique), `description`, `price`, `discount_price`, `category_id` (FK), `featured`, `active`, `created_at`.
- **`product_variants`**: `id` (PK), `product_id` (FK), `sku` (Unique), `size` (S, M, L, XL), `color`, `stock_quantity`.
- **`product_images`**: `id` (PK), `product_id` (FK), `image_url` (Cloudinary), `is_primary`.
- **`reviews`**: `id` (PK), `product_id` (FK), `user_id`, `rating` (1-5 stars), `comment`, `created_at`.

### 2.3. Cart Service Storage (Redis Key-Value)
- **Data Structure**: Redis Hash / JSON Object
- **Key Pattern**: `cart:{userId}`
- **TTL**: 30 ngày (`2,592,000` seconds)
- **JSON Structure**:
  ```json
  {
    "userId": 102,
    "items": [
      {
        "productId": 42,
        "variantId": 105,
        "productName": "Áo Polo Kyro Modern",
        "size": "L",
        "color": "Black",
        "price": 350000.0,
        "quantity": 2,
        "imageUrl": "https://res.cloudinary.com/..."
      }
    ],
    "totalPrice": 700000.0,
    "updatedAt": "2026-08-02T21:00:00"
  }
  ```

### 2.4. Order Service Database (`kyro_order`)
- **`orders`**: `id` (PK), `order_number` (Unique), `user_id`, `user_email`, `total_amount`, `shipping_fee`, `discount_amount`, `status` (`PENDING`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`), `payment_method` (`COD`, `VNPAY`), `payment_status` (`UNPAID`, `PAID`), `created_at`.
- **`order_items`**: `id` (PK), `order_id` (FK), `product_id`, `product_name`, `variant_id`, `size`, `color`, `price`, `quantity`, `subtotal`.
- **`order_addresses`**: `id` (PK), `order_id` (FK One-to-One), `recipient_name`, `phone_number`, `street_address`, `district`, `city`.
- **`payment_details`**: `id` (PK), `order_id` (FK), `transaction_id`, `payment_method`, `amount`, `status`, `paid_at`.

### 2.5. Payment Service Database (`kyro_payment`)
- **`payment_transactions`**: `id` (PK), `order_id`, `txn_ref` (Unique VNPay Reference), `amount`, `bank_code`, `card_type`, `order_info`, `vnp_response_code`, `status` (`SUCCESS`, `FAILED`, `PENDING`), `created_at`.

### 2.6. AI Service Database (`postgres` + Extension `pgvector`)
- **Extension**: `CREATE EXTENSION IF NOT EXISTS vector;`
- **`product_embeddings`**:
  - `id`: BigInteger (PK)
  - `product_id`: BigInteger (Unique FK to Catalog Product)
  - `title`: String
  - `description`: Text
  - `category_name`: String
  - `embedding`: `vector(768)` *(Vector 768 chiều từ Gemini `models/embedding-001`)*
  - `created_at`: Timestamp

---

## 🛠️ 3. Quản Lý Migration Vớ Flyway & Alembic

1. **Flyway (Java Spring Boot Services)**:
   - Các file SQL migration nằm trong `src/main/resources/db/migration/V1__init_schema.sql`, `V2__add_indexes.sql`.
   - Flyway tự động khởi chạy khi khởi động service, tạo bảng `flyway_schema_history` để theo dõi phiên bản.
2. **Alembic (Python AI Service)**:
   - Sử dụng `alembic upgrade head` tự động chạy trong câu lệnh Docker container startup để sync schema database.
