package com.kyro.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
  List<Image> findByProductId(Long productId);

  void deleteByProductId(Long productId);
}
