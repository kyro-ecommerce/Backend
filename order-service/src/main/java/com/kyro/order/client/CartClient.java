package com.kyro.order.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/** Feign client to communicate with Cart Service. */
@FeignClient(name = "cart-service")
public interface CartClient {

  @GetMapping("/api/v1/carts")
  CartResponse getCart(@RequestHeader("X-User-Id") Long userId);

  @DeleteMapping("/api/v1/carts/items")
  void clearCart(@RequestHeader("X-User-Id") Long userId);

  record CartResponse(
      String userId, List<CartItemResponse> items, int totalPrice, int totalDiscountedPrice) {}

  record CartItemResponse(
      String id,
      Long productId,
      String productName,
      String productImageUrl,
      int quantity,
      int price,
      String size,
      Integer discountPercent,
      Integer discountedPrice) {}
}
