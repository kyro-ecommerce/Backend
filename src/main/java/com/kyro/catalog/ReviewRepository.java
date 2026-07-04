package com.kyro.catalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Dùng tên phương thức tự động tạo query (findBy...)
    List<Review> findAllByProductIdOrderByCreatedAtDesc(Long productId);
    List<Review> findAllByProductId(Long productId);

    // Dùng tên phương thức tự động tạo query (countBy...)
    Integer countByProductId(Long productId);

    // *** Đảm bảo phương thức này có @Query ***
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);

    // Phương thức xóa theo userId (nếu bạn cần) - Spring Data JPA tự tạo query
    void deleteByUserId(Long userId); // Tên này đúng quy ước

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Đếm số lượng đánh giá của một người dùng cho một sản phẩm cụ thể
    Long countByUserIdAndProductId(Long userId, Long productId);
}