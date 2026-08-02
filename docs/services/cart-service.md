# 🛒 Cart Service Documentation

> **Service Name**: `cart-service`  
> **Port**: `8083`  
> **Database / Cache**: Redis 7 (`kyro-redis:6379`)  
> **Integration**: Catalog Client (Feign HTTP)  
> **Package**: `com.kyro`

---

## 📌 1. Chức Năng Chính

**Cart Service** quản lý trạng thái giỏ hàng tạm thời cho người dùng với hiệu năng cực cao nhờ sử dụng **Redis Key-Value Cache**:

1. **Lưu Trữ Giỏ Hàng Trạng Thái Siêu Nhanh (High Performance In-Memory Cart)**:
   - Toàn bộ dữ liệu giỏ hàng của từng người dùng được lưu trữ dạng JSON String trong Redis dưới key `cart:{userId}`.
   - Thời gian sống (TTL) của giỏ hàng được đặt là **30 ngày** (tự động gia hạn mỗi khi người dùng thao tác).
2. **Kiểm Tra Sản Phẩm Qua Feign Client**:
   - Khi thêm item vào giỏ, `CartService` gọi `CatalogClient` qua Feign HTTP để lấy thông tin sản phẩm (tên, giá, hình ảnh) và xác minh số lượng tồn kho còn đủ hay không.
3. **Cập Nhật & Xóa Giỏ Hàng**:
   - Thêm sản phẩm, thay đổi số lượng, xóa 1 item, hoặc xóa toàn bộ giỏ hàng (`clearCart`) sau khi đặt hàng thành công.

---

## 🗄️ 2. Structure Cấu Trúc Dữ Liệu Redis

Key Pattern: `cart:{userId}` (Ví dụ `cart:102`)

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
| `GET` | `/api/v1/cart` | Lấy toàn bộ thông tin giỏ hàng của người dùng | User / Admin |
| `POST` | `/api/v1/cart/items` | Thêm sản phẩm vào giỏ (gọi CatalogClient kiểm tra tồn kho) | User / Admin |
| `PUT` | `/api/v1/cart/items/{variantId}` | Cập nhật số lượng sản phẩm trong giỏ | User / Admin |
| `DELETE` | `/api/v1/cart/items/{variantId}` | Xóa 1 sản phẩm khỏi giỏ hàng | User / Admin |
| `DELETE` | `/api/v1/cart/clear` | Xóa sạch giỏ hàng của người dùng | User / Admin / Feign |
