package com.kyro.catalog;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/admin/analytics/products")
public class AdminProductAnalyticsController {
  private final ProductService productService;

  public AdminProductAnalyticsController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/summary")
  public ResponseEntity<Map<String, Object>> getSummary() {
    return ResponseEntity.ok(productService.getAdminFilterStatistics());
  }

  @GetMapping("/top-selling")
  public ResponseEntity<List<Map<String, Object>>> getTopSelling(
      @RequestParam(defaultValue = "10") int limit) {
    return ResponseEntity.ok(productService.getTopSellingProducts(limit));
  }

  @GetMapping("/revenue-by-category")
  public ResponseEntity<Map<String, Object>> getRevenueByCategory() {
    return ResponseEntity.ok(productService.getRevenueByCateogry());
  }
}
