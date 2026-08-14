package com.kyro.catalog;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select v from ProductVariant v where v.id=:id")
  Optional<ProductVariant> findByIdWithLock(@Param("id") Long id);
}
