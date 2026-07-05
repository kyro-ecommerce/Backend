package com.kyro.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  // Spring Data JPA auto query derivation (findBy...)
  List<Review> findAllByProductIdOrderByCreatedAtDesc(Long productId);

  List<Review> findAllByProductId(Long productId);

  // Spring Data JPA auto query derivation (countBy...)
  Integer countByProductId(Long productId);

  // *** Ensure this method has @Query ***
  @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId")
  Double calculateAverageRatingByProductId(@Param("productId") Long productId);

  // Delete by userId auto query
  void deleteByUserId(Long userId); // Correct method naming convention

  boolean existsByUserIdAndProductId(Long userId, Long productId);

  // Count reviews by a specific user for a specific product
  Long countByUserIdAndProductId(Long userId, Long productId);
}
