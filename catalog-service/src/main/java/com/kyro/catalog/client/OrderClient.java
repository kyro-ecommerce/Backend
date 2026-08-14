package com.kyro.catalog.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Feign client to communicate with the Order Service. */
@FeignClient(name = "order-service")
public interface OrderClient {

  @GetMapping("/api/v1/internal/orders/top-selling")
  List<TopSellingProductResponse> getTopSellingProducts(@RequestParam("limit") int limit);
  @GetMapping("/api/v1/internal/orders/product-revenue")
  List<ProductRevenueResponse> getProductRevenue();

  /**
   * Verifies if a user has purchased a product and it was successfully delivered.
   *
   * @param userId user ID
   * @param productId product ID
   * @return true if purchased and delivered, false otherwise
   */
  @GetMapping("/api/v1/internal/orders/purchases")
  boolean hasPurchasedAndDelivered(
      @RequestParam("userId") Long userId, @RequestParam("productId") Long productId);

  record TopSellingProductResponse(Long productId, Long quantitySold) {}
  record ProductRevenueResponse(Long productId, Long revenue) {}
}
