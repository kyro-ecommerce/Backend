package com.kyro.order.client;

import java.util.List;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/** Feign client to communicate with Cart Service. */
@FeignClient(name = "cart-service")
public interface CartClient {

  @GetMapping("/api/v1/cart")
  CartResponse getCart(@RequestHeader("X-User-Id") Long userId);

  @DeleteMapping("/api/v1/cart")
  void clearCart(@RequestHeader("X-User-Id") Long userId);

  @Data
  class CartResponse {
    private String userId;
    private List<CartItemResponse> items;
    private int totalPrice;
    private int totalDiscountedPrice;
  }

  @Data
  class CartItemResponse {
    private String id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private int quantity;
    private int price;
    private String size;
    private Integer discountPercent;
    private Integer discountedPrice;
  }
}
