package com.kyro.order.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/** Feign client to communicate with Cart Service. */
@FeignClient(name = "cart-service")
public interface CartClient {

  @GetMapping("/api/v1/internal/carts/{userId}")
  CartResponse getCart(@PathVariable("userId") Long userId);

  @PostMapping("/api/v1/internal/carts/{userId}/selection")
  CartResponse getSelection(@PathVariable("userId") Long userId, @RequestBody CartSelectionRequest request);

  @DeleteMapping("/api/v1/internal/carts/{userId}")
  void clearCart(@PathVariable("userId") Long userId);

  record CartResponse(
      String userId, long version, List<CartItemResponse> items, int totalPrice, int totalDiscountedPrice) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record CartItemResponse(
      Long id,
      Long productId,
      String productName,
      String productImageUrl,
      int quantity,
      int price,
      String size,
      Integer discountPercent,
      Integer discountedPrice) {}

  record CartSelectionRequest(List<Long> cartItemIds) {}
}
