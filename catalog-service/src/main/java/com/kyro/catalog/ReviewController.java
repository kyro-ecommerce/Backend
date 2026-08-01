package com.kyro.catalog;

import com.kyro.catalog.client.UserClient;
import com.kyro.catalog.dto.ReviewDTO;
import com.kyro.catalog.dto.ReviewRequest;
import com.kyro.exceptions.DomainException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Controller for product review operations. */
@RestController
@RequestMapping("${api.prefix}/reviews")
public class ReviewController {

  private final ReviewService reviewService;
  private final UserClient userClient;
  private final ProductService productService;

  public ReviewController(
      ReviewService reviewService, UserClient userClient, ProductService productService) {
    this.reviewService = reviewService;
    this.userClient = userClient;
    this.productService = productService;
  }

  /**
   * Creates a new review for a product. Reads userId from request headers injected by gateway, and
   * fetches user name via UserClient.
   */
  @PostMapping("/create")
  public ResponseEntity<ReviewDTO> createReview(
      @RequestHeader("X-User-Id") Long userId, @RequestBody ReviewRequest reviewRequest) {

    UserClient.UserResponse user = userClient.getUserById(userId);
    if (user == null) {
      throw new DomainException(HttpStatus.UNAUTHORIZED, "User details not found");
    }

    Review res =
        reviewService.createReview(userId, user.firstName(), user.lastName(), reviewRequest);
    if (res == null) {
      throw new DomainException(HttpStatus.BAD_REQUEST, "Failed to create review");
    }

    ReviewDTO reviewDTO = new ReviewDTO(res);
    return ResponseEntity.ok(reviewDTO);
  }

  /** Retrieves all reviews for a product. */
  @GetMapping("/product/{productId}")
  public ResponseEntity<Map<String, Object>> getProductReview(@PathVariable Long productId) {
    List<Review> reviews = reviewService.getReviewsByProductId(productId);
    Product product = productService.findProductById(productId);

    Map<Integer, Long> ratingDistribution =
        reviews.stream().collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

    Map<String, Long> finalDistribution = new HashMap<>();
    for (int i = 5; i >= 1; i--) {
      finalDistribution.put(String.valueOf(i), ratingDistribution.getOrDefault(i, 0L));
    }

    Map<String, Object> resultData = new HashMap<>();
    resultData.put("productId", productId);
    resultData.put("productName", product.getTitle());
    resultData.put("averageRating", product.getAverageRating());
    resultData.put("totalReviews", product.getNumRatings());
    resultData.put("ratingDistribution", finalDistribution);

    List<ReviewDTO> reviewDTOs = reviews.stream().map(ReviewDTO::new).toList();
    resultData.put("reviews", reviewDTOs);

    return ResponseEntity.ok(resultData);
  }

  /** Updates an existing review. */
  @PutMapping("/update/{reviewId}")
  public ResponseEntity<ReviewDTO> updateReview(
      @RequestHeader("X-User-Id") Long userId,
      @PathVariable Long reviewId,
      @RequestBody ReviewRequest reviewRequest) {

    Review res = reviewService.updateReview(reviewId, reviewRequest, userId);

    if (res == null) {
      throw new DomainException(HttpStatus.BAD_REQUEST, "Failed to update review");
    }

    ReviewDTO reviewDTO = new ReviewDTO(res);
    return ResponseEntity.ok(reviewDTO);
  }

  /** Deletes a review. */
  @DeleteMapping("/delete/{reviewId}")
  public ResponseEntity<Map<String, String>> deleteReview(
      @RequestHeader("X-User-Id") Long userId, @PathVariable Long reviewId) {

    reviewService.deleteReview(reviewId, userId);
    return ResponseEntity.ok(Map.of("message", "Delete Review Success!"));
  }

  /** Gets a review by its ID. */
  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long reviewId) {
    Review res = reviewService.getReviewById(reviewId);
    if (res == null) {
      throw new DomainException(HttpStatus.BAD_REQUEST, "Review not found");
    }

    ReviewDTO reviewDTO = new ReviewDTO(res);
    return ResponseEntity.ok(reviewDTO);
  }

  /** Checks if a user is eligible to review the product. */
  @GetMapping("/can-review/{productId}")
  public ResponseEntity<Boolean> canUserReviewProduct(
      @RequestHeader("X-User-Id") Long userId, @PathVariable Long productId) {

    boolean canReview = reviewService.canUserReviewProduct(userId, productId);
    return ResponseEntity.ok(canReview);
  }
}
