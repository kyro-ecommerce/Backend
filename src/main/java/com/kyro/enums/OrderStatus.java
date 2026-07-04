package com.kyro.enums;

public enum OrderStatus {
    PENDING, // chờ xác nhận hoặc thanh toán
    CONFIRMED, // đơn hàng đã được xác nhận
    SHIPPED, // Đơn hàng đã được gửi đi
    DELIVERED, // Đơn hàng đã được giao thành công
    CANCELLED // Đơn hàng đã bị hủy
}

