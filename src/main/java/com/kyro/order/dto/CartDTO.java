package com.kyro.order.dto;

import com.kyro.order.Cart;



import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartDTO {
    private Long id;
    private int totalOriginalPrice;      // Tổng giá gốc
    private int totalItems;      // Tổng số lượng sản phẩm
    private int totalDiscountedPrice;  // Tổng giá sau giảm giá
    private int discount;  // Tổng tiền giảm giá
    private List<CartItemDTO> cartItems;

    public CartDTO(Cart cart) {
        this.id = cart.getId();
        this.totalItems = cart.getTotalItems();
        this.totalDiscountedPrice = cart.getTotalDiscountedPrice();
        this.totalOriginalPrice = cart.getOriginalPrice();
        this.discount = cart.getDiscount();

        if (cart.getCartItems() != null) {
            this.cartItems = cart.getCartItems().stream()
                    .map(CartItemDTO::new)
                    .collect(Collectors.toList());
        }

    }
}