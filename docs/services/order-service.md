# 📦 Order Service Documentation

> **Service Name**: `order-service`  
> **Port**: `8085`  
> **Database**: PostgreSQL (`kyro_order`)  
> **Integration**: Catalog Client, Cart Client, Auth Client (Feign HTTP), RabbitMQ (`order.exchange`)  
> **Package**: `com.kyro`

---

## 📌 1. Chức Năng Chính

**Order Service** xử lý quy trình đặt hàng, tính toán giá trị đơn hàng, quản lý vòng đời trạng thái đơn hàng:

1. **Xử Lý Quy Trình Đặt Hàng (Checkout Workflow)**:
   - Tiếp nhận yêu cầu đặt hàng từ client (`addressId`, `paymentMethod`).
   - Gọi `AuthClient` trích xuất và xác thực thông tin địa chỉ giao hàng (`Address`).
   - Gọi `CartClient` lấy danh sách sản phẩm trong giỏ hàng.
   - Kiểm tra và trừ số lượng tồn kho từng sản phẩm qua `CatalogClient`.
   - Lưu thông tin đơn hàng, chi tiết đơn hàng, snapshot địa chỉ giao hàng vào PostgreSQL `kyro_order`.
   - Gọi `CartClient.clearCart(...)` để giải phóng giỏ hàng của người dùng.
2. **Quản Lý Vòng Đời Trạng Thái Đơn Hàng (Order State Machine)**:
   - Các trạng thái: `PENDING` ➔ `CONFIRMED` ➔ `SHIPPING` ➔ `DELIVERED` / `CANCELLED`.
   - Admin có quyền duyệt đơn, cập nhật trạng thái vận chuyển.
3. **Phát Sự Kiện Email Qua RabbitMQ**:
   - Ngay sau khi lưu đơn hàng thành công, `OrderService` phát sự kiện `order.created` lên RabbitMQ `order.exchange`.
   - `NotificationService` lắng nghe sự kiện này và tự động gửi email hóa đơn chi tiết cho người dùng.

---

## 🗄️ 2. Structure & Entities

- **`Order`**: `id`, `orderNumber` (dạng `ORD-YYYYMMDD-XXXX`), `userId`, `userEmail`, `totalAmount`, `shippingFee`, `discountAmount`, `status`, `paymentMethod`, `paymentStatus`, `createdAt`.
- **`OrderItem`**: `id`, `orderId`, `productId`, `productName`, `variantId`, `size`, `color`, `price`, `quantity`, `subtotal`.
- **`OrderAddress`**: `id`, `orderId`, `recipientName`, `phoneNumber`, `streetAddress`, `district`, `city`.
- **`PaymentDetails`**: `id`, `orderId`, `transactionId`, `paymentMethod`, `amount`, `status`, `paidAt`.

---

## 📡 3. Danh Sách REST Endpoints Chính

| Method | Endpoint | Description | Permitted Roles |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/orders` | Đặt hàng từ giỏ hàng hiện tại (Checkout) | User / Admin |
| `GET` | `/api/v1/orders` | Lấy danh sách đơn hàng của người dùng đang đăng nhập | User / Admin |
| `GET` | `/api/v1/orders/{id}` | Lấy chi tiết đơn hàng theo ID | User / Admin |
| `PUT` | `/api/v1/orders/{id}/cancel` | Hủy đơn hàng (nếu đang ở trạng thái PENDING) | User / Admin |
| `GET` | `/api/v1/admin/orders` | Quản lý lấy tất cả đơn hàng toàn hệ thống | Admin |
| `PUT` | `/api/v1/admin/orders/{id}/status` | Cập nhật trạng thái đơn hàng (CONFIRMED, SHIPPING, DELIVERED) | Admin |

---

## 🐰 4. Event Publisher (RabbitMQ Order Created)

```java
public void publishOrderCreatedEvent(Order order) {
    OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .orderNumber(order.getOrderNumber())
            .userEmail(order.getUserEmail())
            .customerName(order.getOrderAddress().getRecipientName())
            .totalAmount(order.getTotalAmount())
            .paymentMethod(order.getPaymentMethod().name())
            .shippingAddress(order.getOrderAddress().getFormattedAddress())
            .items(order.getOrderItems().stream().map(item -> new OrderItemDto(
                    item.getProductName(), item.getQuantity(), item.getPrice(), item.getSubtotal()
            )).collect(Collectors.toList()))
            .build();

    rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
}
```
