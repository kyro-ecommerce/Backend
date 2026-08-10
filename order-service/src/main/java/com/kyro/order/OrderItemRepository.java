package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.order.dto.TopSellingProductResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findByProductId(Long productId);

  @Query(
      "SELECT new TopSellingProductResponse(oi.productId, SUM(oi.quantity)) "
          + "FROM OrderItem oi WHERE oi.order.orderStatus = :status GROUP BY oi.productId "
          + "ORDER BY SUM(oi.quantity) DESC, oi.productId DESC")
  List<TopSellingProductResponse> findTopSellingProducts(
      @Param("status") OrderStatus status, Pageable pageable);

  @Modifying
  @Query(value = "DELETE FROM order_item WHERE order_id = :id", nativeQuery = true)
  void deleteByOrderId(Long id);

  @Modifying
  @Query("DELETE FROM OrderItem oi WHERE oi.order.userId = :userId")
  void deleteByOrderUserId(@Param("userId") Long userId);
}
