package com.kyro.cart.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

  @GetMapping("/api/v1/internal/products/{productId}")
  ProductResponse getProductById(@PathVariable("productId") Long productId);

  @org.springframework.web.bind.annotation.PostMapping("/api/v1/internal/products/lookup")
  List<ProductResponse> getProducts(@org.springframework.web.bind.annotation.RequestBody ProductLookupRequest request);

  record ProductResponse(
      Long id,
      String title,
      int price,
      int discountPersent,
      int discountedPrice,
      String color,
      List<ImageResponse> images, List<SizeResponse> sizes) {}

  record ImageResponse(Long id, String downloadUrl) {}
  record SizeResponse(String name, Integer quantity) {}
  record ProductLookupRequest(List<Long> productIds) {}
}
