package com.kyro.order.dto;

import com.kyro.enums.PaymentStatus;

public record PaymentStatusUpdateRequest(PaymentStatus status) {}
