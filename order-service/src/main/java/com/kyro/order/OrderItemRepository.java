package com.kyro.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
  List<OrderItem> findByProductId(Long productId);

  @Modifying
  @Query(value = "DELETE FROM order_item WHERE order_id = :id", nativeQuery = true)
  void deleteByOrderId(Long id);

  @Modifying
  @Query("DELETE FROM OrderItem oi WHERE oi.order.userId = :userId")
  void deleteByOrderUserId(@Param("userId") Long userId);
}
