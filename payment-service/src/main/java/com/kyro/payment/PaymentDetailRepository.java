package com.kyro.payment;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
  void deleteById(Long id);

  Optional<PaymentDetail> findByOrderId(Long orderId);
}
