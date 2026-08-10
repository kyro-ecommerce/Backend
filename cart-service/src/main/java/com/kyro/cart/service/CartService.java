package com.kyro.cart.service;

import com.kyro.cart.*;
import com.kyro.cart.client.CatalogClient;
import com.kyro.cart.dto.CartDTO;
import com.kyro.cart.dto.CartItemDTO;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {
  private static final String CART_KEY_PREFIX = "cart:";
  private final RedisTemplate<String, Object> redisTemplate;
  private final CatalogClient catalogClient;
  private final CartRepository cartRepository;
  private final ProcessedCartEventRepository processedEventRepository;

  public CartService(
      RedisTemplate<String, Object> redisTemplate,
      CatalogClient catalogClient,
      CartRepository cartRepository,
      ProcessedCartEventRepository processedEventRepository) {
    this.redisTemplate = redisTemplate;
    this.catalogClient = catalogClient;
    this.cartRepository = cartRepository;
    this.processedEventRepository = processedEventRepository;
  }

  public CartDTO getCart(String userId) {
    CartDTO cached = readCache(userId);
    CartDTO cart = refresh(cached != null ? cached : loadCart(Long.valueOf(userId)));
    writeCache(userId, cart);
    return cart;
  }

  @Transactional
  public CartDTO addItemToCart(String userId, CartItemDTO request) {
    if (request.getProductId() == null || request.getQuantity() < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm và số lượng hợp lệ là bắt buộc.");
    }
    CatalogClient.ProductResponse product = getProduct(request.getProductId());
    requireAvailable(product, request.getSize(), request.getQuantity());
    Cart cart = lockedCart(Long.valueOf(userId));
    CartItem item =
        cart.getItems().stream()
            .filter(i -> i.getProductId().equals(request.getProductId()) && Objects.equals(i.getSize(), request.getSize()))
            .findFirst()
            .orElseGet(
                () -> {
                  CartItem created = new CartItem();
                  created.setCart(cart);
                  created.setProductId(request.getProductId());
                  created.setSize(request.getSize());
                  cart.getItems().add(created);
                  return created;
                });
    int quantity = item.getQuantity() + request.getQuantity();
    requireAvailable(product, request.getSize(), quantity);
    applyProduct(item, product);
    item.setQuantity(quantity);
    cart.touch();
    cartRepository.saveAndFlush(cart);
    evict(userId);
    return refresh(toDto(cart));
  }

  @Transactional
  public CartDTO updateCartItem(String userId, Long itemId, int quantity) {
    if (quantity < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng phải lớn hơn 0.");
    Cart cart = lockedCart(Long.valueOf(userId));
    CartItem item = findItem(cart, itemId);
    CatalogClient.ProductResponse product = getProduct(item.getProductId());
    requireAvailable(product, item.getSize(), quantity);
    applyProduct(item, product);
    item.setQuantity(quantity);
    cart.touch();
    cartRepository.saveAndFlush(cart);
    evict(userId);
    return refresh(toDto(cart));
  }

  @Transactional
  public CartDTO removeItemFromCart(String userId, Long itemId) {
    Cart cart = lockedCart(Long.valueOf(userId));
    cart.getItems().remove(findItem(cart, itemId));
    cart.touch();
    cartRepository.saveAndFlush(cart);
    evict(userId);
    return toDto(cart);
  }

  @Transactional
  public void clearCart(String userId) {
    Cart cart = lockedCart(Long.valueOf(userId));
    cart.getItems().clear();
    cart.touch();
    cartRepository.saveAndFlush(cart);
    evict(userId);
  }

  public CartDTO getSelection(String userId, List<Long> itemIds) {
    if (itemIds == null || itemIds.isEmpty() || new HashSet<>(itemIds).size() != itemIds.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách item cần checkout không hợp lệ.");
    }
    CartDTO cart = getCart(userId);
    Set<Long> ids = new HashSet<>(itemIds);
    List<CartItemDTO> selected = cart.getItems().stream().filter(i -> ids.contains(i.getId())).toList();
    if (selected.size() != ids.size()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Có item không thuộc giỏ hàng.");
    if (selected.stream().anyMatch(i -> !i.isAvailable())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Có sản phẩm không còn đủ hàng.");
    }
    CartDTO result = new CartDTO();
    result.setUserId(cart.getUserId());
    result.setVersion(cart.getVersion());
    result.setItems(new ArrayList<>(selected));
    result.calculateTotalAmount();
    return result;
  }

  @Transactional
  public void removePurchasedItems(Long userId, Long orderId, Map<Long, Integer> quantities) {
    if (processedEventRepository.existsById(orderId)) return;
    Cart cart = lockedCart(userId);
    for (CartItem item : new ArrayList<>(cart.getItems())) {
      Integer ordered = quantities.get(item.getId());
      if (ordered == null) continue;
      if (item.getQuantity() <= ordered) cart.getItems().remove(item);
      else item.setQuantity(item.getQuantity() - ordered);
    }
    cart.touch();
    processedEventRepository.save(new ProcessedCartEvent(orderId));
    cartRepository.saveAndFlush(cart);
    evict(userId.toString());
  }

  private Cart lockedCart(Long userId) {
    return cartRepository.findWithItemsForUpdateByUserId(userId).orElseGet(() -> createCart(userId));
  }

  private Cart createCart(Long userId) {
    Cart cart = new Cart();
    cart.setUserId(userId);
    return cartRepository.saveAndFlush(cart);
  }

  private CartDTO loadCart(Long userId) {
    return toDto(cartRepository.findWithItemsByUserId(userId).orElseGet(() -> createCart(userId)));
  }

  private CartItem findItem(Cart cart, Long productId, String size) {
    return cart.getItems().stream()
        .filter(i -> i.getProductId().equals(productId) && Objects.equals(i.getSize(), size))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy item trong giỏ."));
  }

  private CartItem findItem(Cart cart, Long itemId) {
    return cart.getItems().stream()
        .filter(i -> i.getId().equals(itemId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy item trong giỏ."));
  }

  private CartDTO refresh(CartDTO cart) {
    if (cart.getItems().isEmpty()) return cart;
    Map<Long, CatalogClient.ProductResponse> products;
    try {
      products = catalogClient.getProducts(new CatalogClient.ProductLookupRequest(
              cart.getItems().stream().map(CartItemDTO::getProductId).distinct().toList()))
          .stream().collect(Collectors.toMap(CatalogClient.ProductResponse::id, p -> p));
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể xác minh sản phẩm.", e);
    }
    for (CartItemDTO item : cart.getItems()) {
      CatalogClient.ProductResponse product = products.get(item.getProductId());
      if (product == null) {
        item.setAvailable(false); item.setUnavailableReason("PRODUCT_NOT_FOUND"); continue;
      }
      int oldPrice = item.getDiscountedPrice() != null ? item.getDiscountedPrice() : item.getPrice();
      int newPrice = product.discountedPrice();
      item.setPriceChanged(oldPrice != newPrice);
      item.setProductName(product.title());
      item.setPrice(product.price());
      item.setDiscountPercent(product.discountPersent());
      item.setDiscountedPrice(product.discountedPrice());
      if (product.images() != null && !product.images().isEmpty()) item.setProductImageUrl(product.images().get(0).downloadUrl());
      CatalogClient.SizeResponse size = product.sizes() == null ? null : product.sizes().stream()
          .filter(s -> Objects.equals(s.name(), item.getSize())).findFirst().orElse(null);
      item.setAvailable(size != null && size.quantity() != null && size.quantity() >= item.getQuantity());
      item.setUnavailableReason(item.isAvailable() ? null : "INSUFFICIENT_STOCK");
    }
    cart.calculateTotalAmount();
    return cart;
  }

  private CartDTO toDto(Cart cart) {
    CartDTO dto = new CartDTO();
    dto.setUserId(cart.getUserId().toString()); dto.setVersion(cart.getVersion());
    for (CartItem item : cart.getItems()) {
      CartItemDTO out = new CartItemDTO();
      out.setId(item.getId()); out.setProductId(item.getProductId()); out.setProductName(item.getProductName());
      out.setProductImageUrl(item.getProductImageUrl()); out.setQuantity(item.getQuantity()); out.setPrice(item.getPrice());
      out.setSize(item.getSize()); out.setDiscountPercent(item.getDiscountPercent()); out.setDiscountedPrice(item.getDiscountedPrice());
      dto.getItems().add(out);
    }
    dto.calculateTotalAmount();
    return dto;
  }

  private void applyProduct(CartItem item, CatalogClient.ProductResponse product) {
    item.setProductName(product.title()); item.setPrice(product.price()); item.setDiscountPercent(product.discountPersent());
    item.setDiscountedPrice(product.discountedPrice());
    if (product.images() != null && !product.images().isEmpty()) item.setProductImageUrl(product.images().get(0).downloadUrl());
  }

  private void requireAvailable(CatalogClient.ProductResponse product, String size, int quantity) {
    if (product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại.");
    boolean available = product.sizes() != null && product.sizes().stream()
        .anyMatch(s -> Objects.equals(s.name(), size) && s.quantity() != null && s.quantity() >= quantity);
    if (!available) throw new ResponseStatusException(HttpStatus.CONFLICT, "Sản phẩm không đủ tồn kho.");
  }

  private CatalogClient.ProductResponse getProduct(Long productId) {
    try {
      return catalogClient.getProductById(productId);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể xác minh sản phẩm.", e);
    }
  }

  private CartDTO readCache(String userId) {
    try {
      Object cached = redisTemplate.opsForValue().get(CART_KEY_PREFIX + userId);
      return cached instanceof CartDTO dto ? dto : null;
    } catch (Exception ignored) { return null; }
  }

  private void evict(String userId) {
    try { redisTemplate.delete(CART_KEY_PREFIX + userId); } catch (Exception ignored) { }
  }

  private void writeCache(String userId, CartDTO cart) {
    try { redisTemplate.opsForValue().set(CART_KEY_PREFIX + userId, cart, 30, TimeUnit.MINUTES); } catch (Exception ignored) { }
  }
}
