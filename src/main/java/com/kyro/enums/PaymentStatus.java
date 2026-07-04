package com.kyro.enums;

/**
 * Enum đại diện cho các trạng thái thanh toán
 */
public enum PaymentStatus {
    /**
     * Thanh toán đang chờ xử lý
     */
    PENDING,
    
    /**
     * Thanh toán đã thành công
     */
    COMPLETED,
    
    /**
     * Thanh toán thất bại
     */
    FAILED,
    
    /**
     * Thanh toán đã bị hủy
     */
    CANCELLED,
    
    /**
     * Thanh toán đã được hoàn tiền
     */
    REFUNDED
} 