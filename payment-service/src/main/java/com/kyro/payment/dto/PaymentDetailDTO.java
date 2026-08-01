package com.kyro.payment.dto;

public record PaymentDetailDTO(
    Long paymentId,
    String paymentMethod,
    String status,
    int amount,
    String vnp_TxnRef,
    String vnp_TransactionNo,
    String vnp_ResponseCode) {}
