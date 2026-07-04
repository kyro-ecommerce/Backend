package com.kyro.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
    void deleteById(Long id);
    Optional<PaymentDetail> findByOrderId(Long orderId);
}
