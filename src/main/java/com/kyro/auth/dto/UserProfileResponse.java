package com.kyro.auth.dto;

import com.kyro.catalog.Review;
import com.kyro.order.dto.CartDTO;
import com.kyro.order.dto.OrderDTO;




import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String mobile;
    private String role;
    private Boolean status;
    private List<AddressDTO> address = new ArrayList<>();
    private List<OrderDTO> orders = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();
    private CartDTO cart;
    private LocalDateTime createdAt;
    private String imageUrl;
    private String oauthProvider;
}

