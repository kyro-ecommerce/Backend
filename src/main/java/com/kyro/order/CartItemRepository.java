package com.kyro.order;

import com.kyro.catalog.Product;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product AND ci.size = :size AND ci.cart.user.id = :userId")
    public CartItem isCartItemExist(@Param("cart") Cart cart, @Param("product") Product product, @Param("size") String size, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    Optional<CartItem> findByCartAndProductAndSize(Cart cart, Product product, String size);
    boolean existsByCartIdAndProductIdAndSize(Long cartId, Long productId, String size);
    void deleteByCartId(Long cartId);

    void deleteByCartUserId(Long userId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.product.id = :productId")
    List<CartItem> findByProductId(@Param("productId") Long productId);}
