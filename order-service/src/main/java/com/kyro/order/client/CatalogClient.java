package com.kyro.order.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/** Feign client to communicate with Catalog Service. */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

  @GetMapping("/api/v1/internal/products/{productId}")
  ProductResponse getProductById(@PathVariable("productId") Long productId);

  @PatchMapping("/api/v1/internal/products/{productId}/stock")
  void adjustStock(
      @PathVariable("productId") Long productId, @RequestBody StockAdjustmentRequest request);

  record StockAdjustmentRequest(String sizeName, int quantityDelta) {}

  record ProductResponse(
      Long id,
      String title,
      int price,
      int discountPersent,
      int discountedPrice,
      String color,
      List<ImageResponse> images,
      List<SizeResponse> sizes) {}

  record ImageResponse(Long id, String downloadUrl) {}

  record SizeResponse(String name, Integer quantity) {}
}
