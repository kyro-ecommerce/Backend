package com.kyro.order.event;

public record StockResultEvent(Long orderId, Long userId, boolean success, String message) {}
