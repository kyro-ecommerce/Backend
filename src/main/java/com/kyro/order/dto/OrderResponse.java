package com.kyro.order.dto;

import com.kyro.auth.dto.AddressDTO;
import com.kyro.payment.dto.PaymentDetailDTO;




import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String status;
    private int totalAmount;
    private int totalDiscountedPrice;
    private int totalItems;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private AddressDTO shippingAddress;
    private List<OrderItemDTO> orderItems;
    private List<PaymentDetailDTO> paymentDetails;
} 