package com.kyro.payment.dto;

public record PaymentDetailDTO(
    Long paymentId,
    String paymentMethod,
    String status,
    long amount,
    String vnp_TxnRef,
    String vnp_TransactionNo,
    String vnp_ResponseCode) {}
