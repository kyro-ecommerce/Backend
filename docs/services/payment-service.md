# 💳 Payment Service Documentation

> **Service Name**: `payment-service`  
> **Port**: `8086`  
> **Database**: PostgreSQL (`kyro_payment`)  
> **Integration**: Cổng Thanh Toán VNPay (Sandbox / Production), Order Client (Feign HTTP)  
> **Package**: `com.kyro`

---

## 📌 1. Chức Năng Chính

**Payment Service** tích hợp cổng thanh toán trực tuyến **VNPay**, sinh URL thanh toán an toàn và xử lý Webhook / IPN Callback từ ngân hàng:

1. **Tạo URL Thanh Toán VNPay (VNPay Payment URL Generation)**:
   - Gọi `OrderClient` lấy thông tin đơn hàng và số tiền cần thanh toán.
   - Tạo mã tham chiếu giao dịch duy nhất (`vnp_TxnRef`).
   - Tính toán chữ ký HMAC SHA-512 với `VNPAY_HASH_SECRET` để chống giả mạo thông số.
   - Trả về đường dẫn VNPay Checkout cho Frontend chuyển hướng người dùng.
2. **Xử Lý IPN Callback & Checksum Verification**:
   - Tiếp nhận Callback từ VNPay khi người dùng hoàn tất thanh toán.
   - Kiểm tra và xác minh chữ ký SHA-512 (`vnp_SecureHash`) gửi kèm từ VNPay.
   - Cập nhật trạng thái giao dịch trong DB `kyro_payment` (`SUCCESS` / `FAILED`).
   - Phản hồi mã định dạng chuẩn VNPay Response (`RspCode: 00`, `Message: Confirm Success`).
3. **Cập Nhật Trạng Thái Đơn Hàng**:
   - Khi thanh toán thành công, gọi `OrderClient` cập nhật `paymentStatus = PAID`.

---

## 🔐 2. Luồng Thanh Toán VNPay Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Gateway as API Gateway (:8080)
    participant Payment as Payment Service (:8086)
    participant Order as Order Service (:8085)
    participant VNPay as VNPay Payment Gateway

    User->>Gateway: POST /api/v1/payment/create-vnpay-url (orderId)
    Gateway->>Payment: Forward Request
    Payment->>Order: Get Order Info (Total Amount)
    Order-->>Payment: Order Details
    Payment->>Payment: Build VNPay Params & Generate HMAC SHA-512 Hash
    Payment-->>User: Return VNPay Checkout URL

    User->>VNPay: User Enters Bank Card & Completes Payment
    VNPay-->>User: Redirect Back to Return URL

    VNPay->>Gateway: GET /api/v1/payment/vnpay-callback (IPN Webhook Params + Hash)
    Gateway->>Payment: Forward Callback
    Payment->>Payment: Verify SHA-512 Checksum Hash
    alt Valid Signature & Success Code (00)
        Payment->>Payment: Save Transaction (Status: SUCCESS)
        Payment->>Order: Mark Order Status PAID
        Payment-->>VNPay: RspCode 00 (Success)
    else Invalid Signature or Failed Code
        Payment->>Payment: Save Transaction (Status: FAILED)
        Payment-->>VNPay: RspCode 01 (Fail)
    end
```

---

## 📡 3. Danh Sách REST Endpoints Chính

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/payment/create-vnpay-url` | Tạo URL thanh toán VNPay cho đơn hàng | User / Admin |
| `GET` | `/api/v1/payment/vnpay-callback` | Webhook IPN Callback nhận kết quả từ VNPay | Public |
| `GET` | `/api/v1/payment/transactions/{orderId}` | Tra cứu lịch sử thanh toán của đơn hàng | User / Admin |
