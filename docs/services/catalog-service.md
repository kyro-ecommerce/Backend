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

- **`Category`**: `id`, `name`, `parentCategoryId`, `level`, `isParent`.
- **`Product`**: `id`, `categoryId` (FK), `title`, `brand`, giá, mô tả và thông số kỹ thuật.
- **`Sizes`**: `id`, `productId` (FK), `name`, `quantity`.
- **`Image`**: `id`, product (FK), `downloadUrl`, `fileName`, `fileType`; file ảnh thực tế thuộc Cloudinary.
- **`Review`**: `id`, `productId` (FK), `userId` *(external reference to Auth)*, `rating`, `reviewContent`, `createdAt`.

---

## 📡 3. Danh Sách REST Endpoints Chính

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | Lấy danh sách sản phẩm (hỗ trợ phân trang, lọc theo category, search) | Public |
| `GET` | `/api/v1/products/{id}` | Lấy chi tiết sản phẩm và danh sách biến thể | Public |
| `POST` | `/api/v1/admin/products` | Tạo sản phẩm mới | Admin |
| `PATCH` | `/api/v1/admin/products/{id}` | Cập nhật một phần sản phẩm | Admin |
| `DELETE` | `/api/v1/admin/products/{id}` | Xóa sản phẩm | Admin |
| `POST` | `/api/v1/admin/product-imports` | Tạo nhiều sản phẩm | Admin |
| `POST` | `/api/v1/admin/categories` | Tạo danh mục cấp 1/2 | Admin |
| `PATCH` | `/api/v1/admin/categories/{id}` | Đổi tên danh mục | Admin |
| `DELETE` | `/api/v1/admin/categories/{id}` | Xóa danh mục rỗng (cha cascade con) | Admin |
| `POST/GET` | `/api/v1/admin/products/{productId}/images` | Tạo/lấy ảnh (multipart hoặc URL JSON) | Admin |
| `DELETE` | `/api/v1/admin/images/{imageId}` | Xóa một ảnh | Admin |
| `GET` | `/api/v1/categories` | Lấy cây danh mục sản phẩm | Public |
| `GET/POST` | `/api/v1/products/{productId}/reviews` | Đọc/tạo đánh giá sản phẩm | Public / User |

---

## 🖼️ 4. Luồng Tạo Sản Phẩm Và Upload Ảnh

1. Gọi `POST /api/v1/admin/products` với JSON sản phẩm và lưu `id` từ response `201 Created`.
2. Với từng ảnh, gọi tuần tự `POST /api/v1/admin/products/{productId}/images` bằng `multipart/form-data` (field `image`) hoặc JSON `{ "url": "https://..." }`.
3. Mỗi sản phẩm có tối đa 10 ảnh. Response ảnh luôn có `imageId`, `fileName`, `downloadUrl`.
4. Dùng `GET /api/v1/admin/products/{productId}/images` để tải lại danh sách ảnh hoặc `DELETE /api/v1/admin/images/{imageId}` để xóa ảnh.

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
