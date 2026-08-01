package com.kyro.catalog.dto;

import com.kyro.catalog.Review;
import java.time.LocalDateTime;

/** Data Transfer Object representing product reviews. */
public class ReviewDTO {
  private Long id;
  private String review;
  private Long productId;
  private Long userId;
  private String userFirstName;
  private String userLastName;
  private LocalDateTime createdAt;
  private Integer rating;

  public ReviewDTO() {}

  public ReviewDTO(Review review) {
    this.id = review.getId();
    this.review = review.getContent();
    this.productId = review.getProduct().getId();
    this.userFirstName = review.getUserFirstName();
    this.userLastName = review.getUserLastName();
    this.createdAt = review.getCreatedAt();
    this.rating = review.getRating();
    this.userId = review.getUserId();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getReview() {
    return review;
  }

  public void setReview(String review) {
    this.review = review;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getUserFirstName() {
    return userFirstName;
  }

  public void setUserFirstName(String userFirstName) {
    this.userFirstName = userFirstName;
  }

  public String getUserLastName() {
    return userLastName;
  }

  public void setUserLastName(String userLastName) {
    this.userLastName = userLastName;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }
}
