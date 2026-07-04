package com.kyro.catalog.dto;

import com.kyro.catalog.Review;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private String review;
    private Long productId;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private LocalDateTime createdAt;
    private Integer rating;

    public ReviewDTO(Review review) {
        this.id = review.getId();
        this.review = review.getContent();
        this.productId = review.getProduct().getId();
        this.userFirstName = review.getUser().getFirstName();
        this.userLastName = review.getUser().getLastName();
        this.createdAt = review.getCreatedAt();
        this.rating = review.getRating();
        this.userId = review.getUser().getId();
    }
} 