package com.kyro.payment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentDetail, Long> {
  Optional<PaymentDetail> findByTransactionId(String transactionId);

  Optional<PaymentDetail> findByOrderId(Long orderId);
}
