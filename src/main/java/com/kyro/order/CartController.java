package com.kyro.order;

import com.kyro.auth.User;
import com.kyro.auth.UserService;
import com.kyro.order.dto.CartDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/cart")
public class CartController {
    private final CartService cartService;

    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<CartDTO> findUserCart(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        Cart cart = cartService.findUserCart(user.getId());
        CartDTO cartDTO = new CartDTO(cart);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addItemToCart(@RequestHeader("Authorization") String jwt,
                                                     @RequestBody AddItemRequest req) {
        User user = userService.findUserByJwt(jwt);
        cartService.addCartItem(user.getId(), req);

        return ResponseEntity.ok(Map.of("message", "Item added to cart successfully"));
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<CartDTO> updateCartItem(@RequestHeader("Authorization") String jwt,
                                                   @PathVariable Long itemId,
                                                   @RequestBody AddItemRequest req) {
        User user = userService.findUserByJwt(jwt);
        Cart cart = cartService.updateCartItem(user.getId(), itemId, req);
        CartDTO cartDTO = new CartDTO(cart);
        return ResponseEntity.ok(cartDTO);
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Map<String, String>> removeCartItem(@RequestHeader("Authorization") String jwt, @PathVariable Long itemId) {
        User user = userService.findUserByJwt(jwt);
        cartService.removeCartItem(user.getId(), itemId);

        return ResponseEntity.ok(Map.of("message", "Item removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(@RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        cartService.clearCart(user.getId());
        
        return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
    }
}
