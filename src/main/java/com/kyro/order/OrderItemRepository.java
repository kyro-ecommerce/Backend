package com.kyro.order;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProductId(Long productId);

    void deleteByOrderId(Long id);

    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.order.user.id = :userId")
    void deleteByOrderUserId(@Param("userId") Long userId);
}
