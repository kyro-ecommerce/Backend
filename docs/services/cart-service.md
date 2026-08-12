# 🛒 Cart Service Documentation

> **Service Name**: `cart-service`  
> **Port**: `8083`  
> **Database / Cache**: PostgreSQL (`kyro_cart`) + Redis 7 cache (`kyro-redis:6379`)
> **Integration**: Catalog Client (Feign HTTP)  
> **Package**: `com.kyro`

---

## 📌 1. Chức Năng Chính

**Cart Service** lưu giỏ hàng bền vững trên PostgreSQL và dùng Redis làm cache đọc:

1. **Lưu Trữ Giỏ Hàng Trạng Thái Siêu Nhanh (High Performance In-Memory Cart)**:
   - PostgreSQL là nguồn dữ liệu chuẩn; Redis key `cart:{userId}` chỉ là cache có thể tái tạo.
   - Giỏ hàng không hết hạn tự động; đăng xuất hoặc Redis restart không làm mất dữ liệu.
2. **Kiểm Tra Sản Phẩm Qua Feign Client**:
   - Khi thêm item vào giỏ, `CartService` gọi `CatalogClient` qua Feign HTTP để lấy thông tin sản phẩm (tên, giá, hình ảnh) và xác minh số lượng tồn kho còn đủ hay không.
3. **Cập Nhật & Xóa Giỏ Hàng**:
   - Thêm sản phẩm, thay đổi số lượng, xóa 1 item, hoặc xóa toàn bộ giỏ hàng (`clearCart`) sau khi đặt hàng thành công.

---

## 🗄️ 2. Structure Cấu Trúc Dữ Liệu

PostgreSQL giữ một `cart` theo người dùng và các `cart_item`; Redis dùng key `cart:{userId}` làm cache.

```json
{
  "userId": 102,
  "items": [
    {
      "productId": 12,
      "variantId": 45,
      "productName": "Áo Sơ Mi Oxford Kyro",
      "size": "M",
      "color": "White",
      "price": 450000.0,
      "quantity": 2,
      "imageUrl": "https://res.cloudinary.com/..."
    }
  ],
  "totalPrice": 900000.0,
  "updatedAt": "2026-08-02T21:10:00"
}
```

---

## 📡 3. Danh Sách REST Endpoints Chính

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/carts` | Lấy toàn bộ thông tin giỏ hàng của người dùng | User / Admin |
| `POST` | `/api/v1/carts/items` | Thêm sản phẩm vào giỏ (gọi CatalogClient kiểm tra tồn kho) | User / Admin |
| `PATCH` | `/api/v1/carts/items/{itemId}` | Cập nhật số lượng với `{ "quantity": n }` | User / Admin |
| `DELETE` | `/api/v1/carts/items/{itemId}` | Xóa 1 sản phẩm khỏi giỏ hàng | User / Admin |
| `DELETE` | `/api/v1/carts/items` | Xóa sạch giỏ hàng của người dùng | User / Admin / Feign |
