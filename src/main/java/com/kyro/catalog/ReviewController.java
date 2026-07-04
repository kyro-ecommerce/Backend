package com.kyro.catalog;

import com.kyro.auth.User;
import com.kyro.auth.UserRepository;
import com.kyro.auth.UserService;
import com.kyro.catalog.dto.ReviewDTO;
import com.kyro.catalog.dto.ReviewRequest;
import com.kyro.exceptions.DomainException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ReviewDTO> createReview(@RequestHeader("Authorization") String jwt, @RequestBody ReviewRequest reviewRequest) {
        if (jwt == null || jwt.isEmpty()) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized - Missing token");
        }

        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized - User not found");
        }

        Review res = reviewService.createReview(user, reviewRequest);
        if (res == null) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Failed to create review");
        }

        ReviewDTO reviewDTO = new ReviewDTO(res);
        return ResponseEntity.ok(reviewDTO);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getProductReview(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        Product product = productService.findProductById(productId);

        Map<Integer, Long> ratingDistribution = reviews.stream()
                .collect(Collectors.groupingBy(
                        Review::getRating,
                        Collectors.counting()
                ));

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

        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(ReviewDTO::new)
                .toList();
        resultData.put("reviews", reviewDTOs);

        return ResponseEntity.ok(resultData);
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Long reviewId, @RequestBody ReviewRequest reviewRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        Review res = reviewService.updateReview(reviewId, reviewRequest, user);

        if (res == null) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Failed to update review");
        }

        ReviewDTO reviewDTO = new ReviewDTO(res);
        return ResponseEntity.ok(reviewDTO);
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok(Map.of("message", "Delete Review Success!"));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long reviewId) {
        Review res = reviewService.getReviewById(reviewId);
        if (res == null) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "Review not found");
        }

        ReviewDTO reviewDTO = new ReviewDTO(res);
        return ResponseEntity.ok(reviewDTO);
    }

    @GetMapping("/can-review/{productId}")
    public ResponseEntity<Boolean> canUserReviewProduct(@PathVariable Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        boolean canReview = reviewService.canUserReviewProduct(user.getId(), productId);
        return ResponseEntity.ok(canReview);
    }
}
