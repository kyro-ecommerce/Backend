package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.order.dto.TopSellingProductResponse;
import com.kyro.order.dto.ProductRevenueResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findByProductId(Long productId);

  @Query(
      "SELECT COUNT(oi) > 0 FROM OrderItem oi "
          + "WHERE oi.order.userId = :userId AND oi.order.orderStatus = :status "
          + "AND oi.productId = :productId")
  boolean existsByOrderUserIdAndStatusAndProductId(
      @Param("userId") Long userId,
      @Param("status") OrderStatus status,
      @Param("productId") Long productId);

  @Query(
      "SELECT new com.kyro.order.dto.TopSellingProductResponse(oi.productId, SUM(oi.quantity)) "
          + "FROM OrderItem oi WHERE oi.order.orderStatus = :status GROUP BY oi.productId "
          + "ORDER BY SUM(oi.quantity) DESC, oi.productId DESC")
  List<TopSellingProductResponse> findTopSellingProducts(
      @Param("status") OrderStatus status, Pageable pageable);

  @Query("SELECT new com.kyro.order.dto.ProductRevenueResponse(oi.productId, SUM(oi.discountedPrice * oi.quantity)) FROM OrderItem oi WHERE oi.order.orderStatus = :status GROUP BY oi.productId")
  List<ProductRevenueResponse> findProductRevenue(@Param("status") OrderStatus status);

  @Modifying
  @Query(value = "DELETE FROM order_item WHERE order_id = :id", nativeQuery = true)
  void deleteByOrderId(Long id);

  @Modifying
  @Query("DELETE FROM OrderItem oi WHERE oi.order.userId = :userId")
  void deleteByOrderUserId(@Param("userId") Long userId);
}
