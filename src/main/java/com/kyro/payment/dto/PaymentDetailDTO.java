package com.kyro.payment.dto;

import lombok.Data;

@Data
public class PaymentDetailDTO {
    private Long paymentId;
    private String paymentMethod;
    private String status;
    private int amount;
    private String vnp_TxnRef;
    private String vnp_TransactionNo;
    private String vnp_ResponseCode;
    
    // Không bao gồm vnp_SecureHash vì lý do bảo mật
} 