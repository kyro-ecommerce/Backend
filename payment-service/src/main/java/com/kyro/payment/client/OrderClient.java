package com.kyro.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/** Feign client to communicate with Order Service. */
@FeignClient(name = "order-service")
public interface OrderClient {

  @GetMapping("/api/v1/internal/orders/{id}")
  OrderResponse getOrderById(@PathVariable("id") Long orderId);

  @PatchMapping("/api/v1/internal/orders/{id}/payment-status")
  void updatePaymentStatus(
      @PathVariable("id") Long orderId, @RequestBody PaymentStatusUpdateRequest request);

  record PaymentStatusUpdateRequest(String status) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record OrderResponse(
      Long id,
      Long userId,
      Integer totalDiscountedPrice,
      String paymentStatus,
      String paymentMethod) {}
}
