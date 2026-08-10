package com.kyro.cart.controller;

import com.kyro.cart.dto.CartDTO;
import com.kyro.cart.dto.CartSelectionRequest;
import jakarta.validation.Valid;
import com.kyro.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/internal/carts")
public class InternalCartController {
  private final CartService cartService;

  public InternalCartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/{userId}")
  public ResponseEntity<CartDTO> getCart(@PathVariable String userId) {
    return ResponseEntity.ok(cartService.getCart(userId));
  }

  @PostMapping("/{userId}/selection")
  public ResponseEntity<CartDTO> getSelection(
      @PathVariable String userId, @Valid @RequestBody CartSelectionRequest request) {
    return ResponseEntity.ok(cartService.getSelection(userId, request.cartItemIds()));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> clearCart(@PathVariable String userId) {
    cartService.clearCart(userId);
    return ResponseEntity.noContent().build();
  }
}
