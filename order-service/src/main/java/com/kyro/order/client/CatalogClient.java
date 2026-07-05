package com.kyro.order.client;

import java.util.List;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Feign client to communicate with Catalog Service. */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

  @GetMapping("/api/v1/products/internal/{productId}")
  ProductResponse getProductById(@PathVariable("productId") Long productId);

  @PostMapping("/api/v1/products/internal/decrease-stock")
  void decreaseStock(
      @RequestParam("productId") Long productId,
      @RequestParam("sizeName") String sizeName,
      @RequestParam("quantity") int quantity);

  @PostMapping("/api/v1/products/internal/increase-stock")
  void increaseStock(
      @RequestParam("productId") Long productId,
      @RequestParam("sizeName") String sizeName,
      @RequestParam("quantity") int quantity);

  @Data
  class ProductResponse {
    private Long id;
    private String title;
    private int price;
    private int discountPersent;
    private int discountedPrice;
    private String color;
    private List<ImageResponse> images;
    private List<SizeResponse> sizes;
  }

  @Data
  class ImageResponse {
    private Long id;
    private String imageUrl;
  }

  @Data
  class SizeResponse {
    private String name;
    private Integer quantity;
  }
}
