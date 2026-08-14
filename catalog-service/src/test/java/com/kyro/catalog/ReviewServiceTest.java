package com.kyro.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kyro.catalog.client.OrderClient;
import com.kyro.catalog.dto.ReviewRequest;
import com.kyro.exceptions.DomainException;
import jakarta.validation.Validation;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
  @Mock ReviewRepository reviewRepository;
  @Mock ProductRepository productRepository;
  @Mock OrderClient orderClient;
  private ReviewService service;

  @BeforeEach
  void setUp() {
    service = new ReviewService(reviewRepository, productRepository, orderClient);
  }

  @Test
  void ownerCanDeleteReview() {
    Product product = new Product();
    product.setId(7L);
    Review review = review(10L, 3L, product);
    when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));
    when(productRepository.findById(7L)).thenReturn(Optional.of(product));
    when(reviewRepository.countByProductId(7L)).thenReturn(0);
    when(reviewRepository.calculateAverageRatingByProductId(7L)).thenReturn(0.0);

    service.deleteReview(10L, 3L);

    verify(reviewRepository).delete(review);
  }

  @Test
  void otherUserCannotUpdateOrDeleteReview() {
    Review review = review(10L, 3L, new Product());
    when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

    DomainException updateError =
        assertThrows(
            DomainException.class, () -> service.updateReview(10L, new ReviewRequest(), 4L));
    DomainException deleteError =
        assertThrows(DomainException.class, () -> service.deleteReview(10L, 4L));

    assertEquals("REVIEW_FORBIDDEN", updateError.getErrorCode());
    assertEquals("REVIEW_FORBIDDEN", deleteError.getErrorCode());
    verify(reviewRepository, never()).save(review);
    verify(reviewRepository, never()).delete(review);
  }

  @Test
  void missingReviewReturnsNotFound() {
    when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

    DomainException error =
        assertThrows(DomainException.class, () -> service.deleteReview(99L, 3L));

    assertEquals("REVIEW_NOT_FOUND", error.getErrorCode());
  }

  @Test
  void reviewContentAllows500ButRejects501Characters() {
    try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
      var request = new ReviewRequest();
      request.setRating(5);
      request.setContent("a".repeat(500));
      assertFalse(validatorFactory.getValidator().validate(request).stream().findAny().isPresent());

      request.setContent("a".repeat(501));
      assertEquals(1, validatorFactory.getValidator().validate(request).size());
    }
  }

  private Review review(Long id, Long userId, Product product) {
    Review review = new Review();
    review.setId(id);
    review.setUserId(userId);
    review.setProduct(product);
    return review;
  }
}
