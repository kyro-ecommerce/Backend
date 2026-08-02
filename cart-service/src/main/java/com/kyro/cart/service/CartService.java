package com.kyro.cart.service;

import com.kyro.cart.client.CatalogClient;
import com.kyro.cart.dto.CartDTO;
import com.kyro.cart.dto.CartItemDTO;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final CatalogClient catalogClient;

  public CartService(RedisTemplate<String, Object> redisTemplate, CatalogClient catalogClient) {
    this.redisTemplate = redisTemplate;
    this.catalogClient = catalogClient;
  }

  private static final String CART_KEY_PREFIX = "cart:";
  private static final long CART_TTL_DAYS = 30; // Cart expires in 30 days of inactivity

  private String getCartKey(String userId) {
    return CART_KEY_PREFIX + userId;
  }

  public CartDTO getCart(String userId) {
    String key = getCartKey(userId);
    Object cachedCart = redisTemplate.opsForValue().get(key);
    if (cachedCart instanceof CartDTO) {
      return (CartDTO) cachedCart;
    }

    // Return new cart if not found
    CartDTO newCart = new CartDTO();
    newCart.setUserId(userId);
    return newCart;
  }

  public CartDTO saveCart(CartDTO cart) {
    String key = getCartKey(cart.getUserId());
    cart.calculateTotalAmount();
    redisTemplate.opsForValue().set(key, cart, CART_TTL_DAYS, TimeUnit.DAYS);
    return cart;
  }

  public CartDTO addItemToCart(String userId, CartItemDTO item) {
    CartDTO cart = getCart(userId);

    // Fetch product details from Catalog Service
    CatalogClient.ProductResponse product = catalogClient.getProductById(item.getProductId());
    if (product == null) {
      throw new RuntimeException("Sản phẩm với ID " + item.getProductId() + " không tồn tại.");
    }

    // Populate item details
    item.setProductName(product.title());
    item.setPrice(product.price());
    item.setDiscountPercent(product.discountPersent());
    item.setDiscountedPrice(product.discountedPrice());
    if (product.images() != null && !product.images().isEmpty()) {
      item.setProductImageUrl(product.images().get(0).downloadUrl());
    }

    // Search for existing item with the same productId AND size
    Optional<CartItemDTO> existingItem =
        cart.getItems().stream()
            .filter(
                i ->
                    i.getProductId().equals(item.getProductId())
                        && Objects.equals(i.getSize(), item.getSize()))
            .findFirst();

    if (existingItem.isPresent()) {
      CartItemDTO existing = existingItem.get();
      existing.setQuantity(existing.getQuantity() + item.getQuantity());
    } else {
      cart.getItems().add(item);
    }

    return saveCart(cart);
  }

  public CartDTO updateCartItem(String userId, Long productId, String size, int quantity) {
    CartDTO cart = getCart(userId);

    cart.getItems().stream()
        .filter(
            i ->
                i.getProductId().equals(productId)
                    && (size == null || Objects.equals(i.getSize(), size)))
        .findFirst()
        .ifPresent(item -> item.setQuantity(quantity));

    return saveCart(cart);
  }

  public CartDTO removeItemFromCart(String userId, Long productId, String size) {
    CartDTO cart = getCart(userId);
    cart.getItems()
        .removeIf(
            item ->
                item.getProductId().equals(productId)
                    && (size == null || Objects.equals(item.getSize(), size)));
    return saveCart(cart);
  }

  public void clearCart(String userId) {
    String key = getCartKey(userId);
    redisTemplate.delete(key);
  }
}
