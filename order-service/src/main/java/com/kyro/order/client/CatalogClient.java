package com.kyro.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "catalog-service")
public interface CatalogClient {
  @PatchMapping("/api/v1/internal/products/variants/{variantId}/stock")
  void adjustStock(
      @PathVariable("variantId") Long variantId, @RequestBody StockAdjustmentRequest request);

  record StockAdjustmentRequest(Long variantId, int quantityDelta) {}
}
