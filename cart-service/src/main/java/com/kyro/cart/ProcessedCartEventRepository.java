package com.kyro.cart;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedCartEventRepository extends JpaRepository<ProcessedCartEvent, Long> {}
