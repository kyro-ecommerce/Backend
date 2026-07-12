package com.kyro.cart.client;

import java.util.List;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

  @GetMapping("/api/v1/products/internal/{productId}")
  ProductResponse getProductById(@PathVariable("productId") Long productId);

  @Data
  class ProductResponse {
    private Long id;
    private String title;
    private int price;
    private int discountPersent;
    private int discountedPrice;
    private String color;
    private List<ImageResponse> images;
  }

  @Data
  class ImageResponse {
    private Long id;
    private String downloadUrl;
  }
}
