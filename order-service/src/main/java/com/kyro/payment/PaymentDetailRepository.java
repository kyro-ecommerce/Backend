package com.kyro.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {

  @Modifying
  @Query(value = "DELETE FROM payment_details WHERE order_id = :orderId", nativeQuery = true)
  void deleteByOrderId(@Param("orderId") Long orderId);
}
