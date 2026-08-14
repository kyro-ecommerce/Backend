package com.kyro.payment.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Feign client to communicate with Order Service. */
@FeignClient(name = "order-service")
public interface OrderClient {

  @GetMapping("/api/v1/internal/orders/{id}")
  OrderResponse getOrderById(@PathVariable("id") Long orderId);

  @JsonIgnoreProperties(ignoreUnknown = true)
  record OrderResponse(
      Long id,
      Long userId,
      Long totalDiscountedPrice,
      String paymentStatus,
      String paymentMethod,
      String orderStatus) {}
}
