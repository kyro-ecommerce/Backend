package com.kyro.auth.dto;

import com.kyro.auth.Address;
import com.kyro.auth.User;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String mobile;
    private boolean active;
    private boolean banned;
    private List<Address> addresses;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String oauthProvider;
    private long orderCount;
    private BigDecimal totalSpent;

    // constructor to map all fields from User entity
    public UserDTO(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole() != null ? user.getRole().getName().toString() : null;
        this.mobile = user.getPhone();
        this.active = user.isActive();
        this.banned = user.isBanned();
        this.addresses = user.getAddress();
        this.createdAt = user.getCreatedAt();
        this.imageUrl = user.getImageUrl();
        this.oauthProvider = user.getOauthProvider();
        this.orderCount = user.getOrders() != null ? user.getOrders().size() : 0;
        this.totalSpent = java.math.BigDecimal.ZERO;
    }
}

