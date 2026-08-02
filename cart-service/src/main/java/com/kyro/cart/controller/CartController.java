package com.kyro.cart.controller;

import com.kyro.cart.dto.CartDTO;
import com.kyro.cart.dto.CartItemDTO;
import com.kyro.cart.service.CartService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"${api.prefix}/carts", "${api.prefix}/cart"})
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping
  public ResponseEntity<CartDTO> getCart(@RequestHeader("X-User-Id") String userId) {
    return ResponseEntity.ok(cartService.getCart(userId));
  }

  @PostMapping({"/items", "/add"})
  public ResponseEntity<CartDTO> addItemToCart(
      @RequestHeader("X-User-Id") String userId, @RequestBody CartItemDTO item) {
    return ResponseEntity.ok(cartService.addItemToCart(userId, item));
  }

  @PutMapping({"/items", "/update"})
  public ResponseEntity<CartDTO> updateCartItem(
      @RequestHeader("X-User-Id") String userId,
      @RequestParam Long productId,
      @RequestParam(required = false) String size,
      @RequestParam int quantity) {
    return ResponseEntity.ok(cartService.updateCartItem(userId, productId, size, quantity));
  }

  @DeleteMapping({"/items/{productId}", "/remove/{productId}"})
  public ResponseEntity<CartDTO> removeItemFromCart(
      @RequestHeader("X-User-Id") String userId,
      @PathVariable Long productId,
      @RequestParam(required = false) String size) {
    return ResponseEntity.ok(cartService.removeItemFromCart(userId, productId, size));
  }

  @DeleteMapping({"", "/items", "/clear"})
  public ResponseEntity<Map<String, String>> clearCart(@RequestHeader("X-User-Id") String userId) {
    cartService.clearCart(userId);
    return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
  }
}
