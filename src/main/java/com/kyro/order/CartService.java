package com.kyro.order;

import com.kyro.auth.User;
import com.kyro.auth.UserService;
import com.kyro.catalog.Product;
import com.kyro.catalog.ProductService;
import com.kyro.catalog.ProductSize;




import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final UserService userService;
    private final CartItemService cartItemService;


        public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private Cart createCart(Long userId)  {
        User user = userService.getUserById(userId);
        return createCart(user);
    }

        public Cart findUserCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            // Tạo giỏ hàng mới nếu chưa có
            cart = createCart(userId);
        }
        return cart;
    }
    
        public Cart getCartByUserId(Long userId) {
        return findUserCart(userId);
    }
    
        public Cart initializeNewCartForUser(User user) {
        Cart existingCart = cartRepository.findByUserId(user.getId());
        if (existingCart != null) {
            return existingCart;
        }
        return createCart(user);
    }

        public Cart addCartItem(Long userId, AddItemRequest req) {
        Cart cart = findUserCart(userId);
        Product product = productService.findProductById(req.getProductId());

        // Kiểm tra sản phẩm có tồn tại trong giỏ hàng chưa
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(req.getProductId())
                        && item.getSize().equals(req.getSize()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Cập nhật số lượng nếu sản phẩm đã tồn tại
            existingItem.setQuantity(existingItem.getQuantity() + req.getQuantity());

            ProductSize productSize = product.getSizes().stream()
                    .filter(item -> item.getName().equals(req.getSize()))
                    .findFirst()
                    .orElse(null);

            if (existingItem.getQuantity() > productSize.getQuantity()) {
                throw new RuntimeException("Số lượng sản phẩm không đủ trong kho");
            }

            cartItemRepository.save(existingItem);
        } else {
            // Thêm sản phẩm mới vào giỏ hàng
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setSize(req.getSize());
            newItem.setQuantity(req.getQuantity());
            newItem.setPrice(product.getPrice());
            newItem.setDiscountedPrice(product.getDiscountedPrice());
            cart.getCartItems().add(newItem);
            newItem.setDiscountPercent(product.getDiscountPersent());
            cartItemRepository.save(newItem);
        }

        updateCartTotals(cart);
        return cartRepository.save(cart);
    }

    // Trong CartServiceImpl.java (hoặc tương đương)
        @Transactional // Đảm bảo các thao tác DB trong cùng transaction
    public Cart updateCartItem(Long userId, Long itemId, AddItemRequest req) { // Giữ AddItemRequest vì Controller đang dùng nó
        // Hoặc tốt hơn là tạo một DTO mới chỉ chứa quantity: CartItemQuantityUpdateRequest
        // public Cart updateCartItem(Long userId, Long itemId, CartItemQuantityUpdateRequest req) {

        // 1. Tìm giỏ hàng của user (vẫn cần thiết)
        Cart cart = findUserCart(userId); // Hàm này cần đảm bảo hoạt động đúng

        // 2. Tìm CartItem cần cập nhật bằng itemId
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId)); // Dùng exception cụ thể hơn

        // 3. Kiểm tra xem CartItem có thuộc về giỏ hàng của user không
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to the current user's cart."); // Dùng exception cụ thể
        }

        // 4. Lấy số lượng mới từ request
        Integer newQuantity = req.getQuantity(); // Lấy quantity từ request DTO
        if (newQuantity == null || newQuantity < 1) {
            throw new RuntimeException("Quantity must be greater than 0."); // Validate số lượng
        }

        // 5. *** QUAN TRỌNG: Kiểm tra số lượng tồn kho ***
        // Cần lấy thông tin Product và ProductSize liên quan đến CartItem *hiện tại*
        // Không nên dựa vào productId/size từ request body (vì nó không được gửi hoặc không cần thiết)
        Product product = item.getProduct(); // Lấy Product từ CartItem
        String itemSizeName = item.getSize(); // Lấy Size từ CartItem

        if (product == null) {
            throw new RuntimeException("Product associated with cart item not found."); // Lỗi dữ liệu nếu product null
        }

        // Tìm ProductSize tương ứng với size của CartItem
        ProductSize productSize = product.getSizes().stream()
                .filter(ps -> ps.getName().equals(itemSizeName))
                .findFirst()
                .orElse(null); // Hoặc .orElseThrow nếu size bắt buộc phải tồn tại

        if (productSize == null) {
            throw new RuntimeException("Size '" + itemSizeName + "' not found for product " + product.getId());
        }

        // Kiểm tra số lượng tồn kho so với số lượng MỚI yêu cầu
        if (newQuantity > productSize.getQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm '" + product.getTitle() + "' size '" + itemSizeName + "' không đủ trong kho (Còn lại: " + productSize.getQuantity() + ")");
        }
        // *************************************************

        // 6. Cập nhật số lượng cho CartItem
        item.setQuantity(newQuantity);
        // Có thể cần cập nhật lại giá nếu giá sản phẩm thay đổi (tùy logic)
        // item.setPrice(product.getPrice());
        // item.setDiscountedPrice(product.getDiscountedPrice());

        // 7. Lưu lại CartItem đã cập nhật
        cartItemRepository.save(item);

        // 8. Cập nhật lại tổng tiền của giỏ hàng
        updateCartTotals(cart); // Hàm này tính lại totalPrice, totalDiscountedPrice...

        // 9. Lưu lại giỏ hàng (không bắt buộc nếu updateCartTotals không thay đổi cart entity)
        // return cartRepository.save(cart);
        return cart; // Trả về cart đã được cập nhật (trong bộ nhớ)
    }

// Đồng thời sửa Controller để nhận DTO chỉ có quantity nếu muốn
// Hoặc giữ nguyên AddItemRequest nhưng chỉ dùng trường quantity của nó trong service.

        public void removeCartItem(Long userId, Long itemId) {
        Cart cart = findUserCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to user");
        }

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);

        updateCartTotals(cart);
        cartRepository.save(cart);
    }

        @Transactional  // Thêm annotation này
    public void clearCart(Long userId) {
        Cart cart = findUserCart(userId);

        // Xóa các mục từ bảng cart_items trước
        cartItemService.deleteAllCartItems(cart.getId(), userId);

        // Cập nhật đối tượng cart
        cart.getCartItems().clear();
        cart.setTotalItems(0);
        cart.setOriginalPrice(0);
        cart.setTotalDiscountedPrice(0);
        cart.setDiscount(0);

        // Lưu giỏ hàng đã được cập nhật
        cartRepository.save(cart);
    }

    private void updateCartTotals(Cart cart) {
        Set<CartItem> items = cart.getCartItems(); // use set

        int totalOriginalPrice = items.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        int totalDiscountedPrice = items.stream()
                .mapToInt(item -> item.getDiscountedPrice() * item.getQuantity())
                .sum();

        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        cart.setOriginalPrice(totalOriginalPrice);
        cart.setTotalDiscountedPrice(totalDiscountedPrice);
        cart.setTotalItems(totalItems);
        cart.setDiscount(totalOriginalPrice - totalDiscountedPrice);
    }
}

