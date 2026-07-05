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

  // Do not include vnp_SecureHash for security reasons
}
