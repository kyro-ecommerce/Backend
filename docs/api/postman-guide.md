# 📮 Postman Collection & Testing Guide

> Hướng dẫn kiểm thử và sử dụng file Postman Collection `kyro_postman_collection.json` tích hợp sẵn trong repository.

---

## 📌 1. Import Collection Vào Postman

1. Mở ứng dụng **Postman**.
2. Nhấn nút **Import** (ở góc trên bên trái).
3. Chọn file [kyro_postman_collection.json](file:///Users/tphuc263/Project/Kyro/backend/kyro_postman_collection.json) trong thư mục root của dự án.

---

## ⚙️ 2. Biến Môi Trường Trong Postman (Environment Variables)

Khuyên dùng cấu hình một Environment trong Postman với các giá trị:

| Variable Name | Initial Value | Current Value |
| :--- | :--- | :--- |
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |
| `token` | *(Tự động lưu sau khi đăng nhập)* | `Bearer eyJhbGciOi...` |
| `userId` | `102` | `102` |

---

## 🧪 3. Quy Trình Test Endpoints Chuẩn

1. **Bước 1: Đăng Ký Tài Khoản Mới**:
   - Request: `POST {{baseUrl}}/api/v1/auth/register`
   - Body JSON gửi thông tin `email`, `password`, `fullName`.
2. **Bước 2: Đăng Nhập Lấy JWT Token**:
   - Request: `POST {{baseUrl}}/api/v1/auth/login`
   - Copy chuỗi Access Token gán vào Header Authorization: `Bearer <token>`.
3. **Bước 3: Xem Danh Sách Sản Phẩm**:
   - Request: `GET {{baseUrl}}/api/v1/products`
4. **Bước 4: Thêm Sản Phẩm Vào Giỏ Hàng**:
   - Request: `POST {{baseUrl}}/api/v1/carts/items`
5. **Bước 5: Đặt Hàng (Checkout)**:
   - Request: `POST {{baseUrl}}/api/v1/orders`
   - Body: `{ "addressId": 1, "paymentMethod": "VNPAY", "cartItemIds": [11], "cartVersion": 0, "expectedTotalDiscountedPrice": 100000 }`
6. **Bước 6: Tạo URL Thanh Toán VNPay**:
   - Request: `POST {{baseUrl}}/api/v1/orders/1/payments`
