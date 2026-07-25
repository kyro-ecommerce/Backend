package com.kyro.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Feign client to communicate with Order Service. */
@FeignClient(name = "order-service")
public interface OrderClient {

  @GetMapping("/api/v1/orders/{id}")
  OrderResponse getOrderById(@PathVariable("id") Long orderId);

  @PutMapping("/api/v1/orders/{id}/payment-status")
  void updatePaymentStatus(@PathVariable("id") Long orderId, @RequestParam("status") String status);

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  class OrderResponse {
    private Long id;
    private Integer totalDiscountedPrice;
    private String paymentStatus;
    private String paymentMethod;
  }
}

