package com.kyro.payment.event;

public record PaymentStatusChangedEvent(Long orderId, String status) {}
