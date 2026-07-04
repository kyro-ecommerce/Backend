package com.kyro.order;

import com.kyro.auth.User;



import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @OneToOne
    @JoinColumn(name="user_id")
    @JsonBackReference
    private User user;

    @Column(name = "total_items")
    private int totalItems;

    @Column(name = "total_discounted_price")
    private int totalDiscountedPrice;

    @Column(name = "original_price")
    private int originalPrice;

    @Column(name = "discount")
    private int discount;

    @OneToMany(mappedBy="cart", cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<CartItem> cartItems = new HashSet<>();
}
