# 🏷️ Catalog Service Documentation

> **Service Name**: `catalog-service`  
> **Port**: `8082`  
> **Database**: PostgreSQL (`kyro_catalog`)  
> **Integration**: Cloudinary API (Lưu trữ ảnh), RabbitMQ (`catalog.exchange`), OpenFeign  
> **Package**: `com.kyro`

---

## 📌 1. Chức Năng Chính

**Catalog Service** là trung tâm quản lý sản phẩm và nội dung hiển thị cho trang e-commerce:

1. **Quản Lý Danh Mục & Sản Phẩm (Categories & Products)**:
   - Danh mục sản phẩm phân cấp đa tầng (Parent-Child Categories).
   - Quản lý sản phẩm, mô tả, giá niêm yết, giá khuyến mãi, trạng thái kích hoạt.
2. **Quản Lý Biến Thể & Tồn Kho (Product Variants & Stock)**:
   - Biến thể sản phẩm theo kích thước (`Size`: S, M, L, XL), màu sắc (`Color`), mã SKU.
   - Quản lý số lượng tồn kho (`stockQuantity`).
   - Cung cấp API cho `OrderService` và `CartService` kiểm tra & trừ số lượng tồn kho.
3. **Upload Ảnh Với Cloudinary**:
   - Tích hợp Cloudinary SDK để tải lên ảnh sản phẩm trực tiếp từ máy admin.
4. **Đánh Giá & Nhận Xét (Reviews & Ratings)**:
   - Cho phép người dùng đánh giá sao (1-5) và bình luận cho sản phẩm.
5. **Phát Sự Kiện Sản Phẩm Cho AI Service**:
   - Mỗi khi sản phẩm được tạo mới, cập nhật hoặc xóa, `CatalogService` phát message `product.created` / `product.updated` lên RabbitMQ `catalog.exchange` để `ai-service` cập nhật vector database (`pgvector`).

---

## 🗄️ 2. Structure & Entities

- **`Category`**: `id`, `name`, `slug`, `parentId`, `imageUrl`.
- **`Product`**: `id`, `name`, `slug`, `description`, `price`, `discountPrice`, `categoryId`, `featured`, `active`.
- **`ProductVariant`**: `id`, `productId`, `sku`, `size`, `color`, `stockQuantity`.
- **`ProductImage`**: `id`, `productId`, `imageUrl`, `isPrimary`.
- **`Review`**: `id`, `productId`, `userId`, `rating`, `comment`, `createdAt`.

---

## 📡 3. Danh Sách REST Endpoints Chính

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | Lấy danh sách sản phẩm (hỗ trợ phân trang, lọc theo category, search) | Public |
| `GET` | `/api/v1/products/{id}` | Lấy chi tiết sản phẩm và danh sách biến thể | Public |
| `POST` | `/api/v1/admin/products` | Tạo sản phẩm mới | Admin |
| `PUT` | `/api/v1/admin/products/{id}` | Cập nhật sản phẩm | Admin |
| `DELETE` | `/api/v1/admin/products/{id}` | Xóa sản phẩm | Admin |
| `POST` | `/api/v1/admin/products/bulk` | Tạo nhiều sản phẩm | Admin |
| `POST` | `/api/v1/images/upload/{productId}` | Upload một ảnh cho sản phẩm | Admin |
| `GET` | `/api/v1/images/product/{productId}` | Lấy danh sách ảnh của sản phẩm | Admin |
| `DELETE` | `/api/v1/images/delete/{imageId}` | Xóa một ảnh | Admin |
| `GET` | `/api/v1/categories` | Lấy cây danh mục sản phẩm | Public |
| `POST` | `/api/v1/reviews` | Đánh giá sản phẩm | User / Admin |

---

## 🖼️ 4. Luồng Tạo Sản Phẩm Và Upload Ảnh

1. Gọi `POST /api/v1/admin/products` với JSON sản phẩm và lưu `id` từ response `201 Created`.
2. Với từng ảnh, gọi `POST /api/v1/images/upload/{productId}` bằng `multipart/form-data`, field `image`.
3. Dùng `GET /api/v1/images/product/{productId}` để tải lại danh sách ảnh hoặc `DELETE /api/v1/images/delete/{imageId}` để xóa ảnh.

Nếu một lần upload lỗi, sản phẩm vẫn được giữ lại; client có thể retry ảnh đó với cùng `productId`. Chi tiết sản phẩm trả danh sách ảnh trong `imageUrls`.

Tất cả endpoint ảnh chỉ đi qua API Gateway và yêu cầu JWT có role `ADMIN`.

---

## 🐰 5. Event Publisher (RabbitMQ Product Sync)

```java
public void publishProductEvent(Product product, String eventType) {
    ProductSyncEvent event = ProductSyncEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .productId(product.getId())
            .title(product.getName())
            .description(product.getDescription())
            .categoryName(product.getCategory().getName())
            .price(product.getPrice())
            .inStock(product.getVariants().stream().anyMatch(v -> v.getStockQuantity() > 0))
            .updatedAt(LocalDateTime.now())
            .build();

    rabbitTemplate.convertAndSend("catalog.exchange", "product." + eventType.toLowerCase(), event);
}
```
