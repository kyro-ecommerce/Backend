package com.kyro.catalog;

import com.kyro.catalog.client.OrderClient;
import com.kyro.catalog.dto.ReviewRequest;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/** Service class for managing product reviews. */
@Service
public class ReviewService {

  private final ReviewRepository reviewRepository;
  private final ProductRepository productRepository;
  private final OrderClient orderClient;

  public ReviewService(
      ReviewRepository reviewRepository,
      ProductRepository productRepository,
      OrderClient orderClient) {
    this.reviewRepository = reviewRepository;
    this.productRepository = productRepository;
    this.orderClient = orderClient;
  }

  /**
   * Creates a new review for a product.
   *
   * @param userId user ID of the reviewer
   * @param firstName first name of the reviewer
   * @param lastName last name of the reviewer
   * @param reviewRequest review details
   * @return the created Review entity
   */
  @Transactional
  public Review createReview(
      Long userId, String firstName, String lastName, Long productId, ReviewRequest reviewRequest) {
    if (!canUserReviewProduct(userId, productId)) {
      throw new IllegalStateException("User is not eligible to review this product.");
    }

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

    Review review = new Review();
    review.setContent(reviewRequest.getContent());
    review.setProduct(product);
    review.setUserId(userId);
    review.setUserFirstName(firstName);
    review.setUserLastName(lastName);
    review.setRating(reviewRequest.getRating());
    review.setCreatedAt(LocalDateTime.now());

    Review savedReview = reviewRepository.save(review);
    updateProductRating(product.getId());

    return savedReview;
  }

  /**
   * Retrieves all reviews for a product.
   *
   * @param productId product ID
   * @return list of reviews
   */
  public List<Review> getReviewsByProductId(Long productId) {
    return reviewRepository.findAllByProductId(productId);
  }

  /**
   * Updates an existing review.
   *
   * @param reviewId review ID
   * @param reviewRequest updated review details
   * @param userId user ID making the update request
   * @return updated Review entity
   */
  @Transactional
  public Review updateReview(Long reviewId, ReviewRequest reviewRequest, Long userId) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

    if (!review.getUserId().equals(userId)) {
      throw new RuntimeException("User not authorized to update this review");
    }

    review.setRating(reviewRequest.getRating());
    review.setContent(reviewRequest.getContent());

    Review updatedReview = reviewRepository.save(review);
    updateProductRating(review.getProduct().getId());

    return updatedReview;
  }

  /**
   * Deletes a review.
   *
   * @param reviewId review ID
   * @param userId user ID making the deletion request
   */
  @Transactional
  public void deleteReview(Long reviewId, Long userId) {
    Review review =
        reviewRepository
            .findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

    if (!review.getUserId().equals(userId)) {
      throw new RuntimeException("User not authorized to delete this review");
    }

    Long productId = review.getProduct().getId();
    reviewRepository.delete(review);
    updateProductRating(productId);
  }

  /**
   * Gets a review by its ID.
   *
   * @param reviewId review ID
   * @return Review entity
   */
  public Review getReviewById(Long reviewId) {
    return reviewRepository
        .findById(reviewId)
        .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
  }

  /**
   * Checks if a user has bought the product and can review it.
   *
   * @param userId user ID
   * @param productId product ID
   * @return true if purchase count > review count
   */
  public boolean canUserReviewProduct(Long userId, Long productId) {
    // Query order-service via FeignClient
    boolean hasPurchased = orderClient.hasPurchasedAndDelivered(userId, productId);
    if (!hasPurchased) {
      return false;
    }

    long reviewCount = reviewRepository.countByUserIdAndProductId(userId, productId);
    // Allows a review if they purchased it but haven't reviewed yet (or purchased multiple times)
    return reviewCount == 0; // Simple check: allowed to review once per product purchased
  }

  private void updateProductRating(Long productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

    Integer newNumRatings = reviewRepository.countByProductId(productId);
    Double newAverageRating = reviewRepository.calculateAverageRatingByProductId(productId);

    product.setNumRatings(newNumRatings != null ? newNumRatings : 0);
    product.setAverageRating(newAverageRating != null ? newAverageRating : 0.0);

    productRepository.save(product);
  }
}
