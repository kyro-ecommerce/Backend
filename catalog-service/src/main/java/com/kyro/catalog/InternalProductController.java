package com.kyro.catalog;

import com.kyro.catalog.dto.ProductInternalResponse;
import com.kyro.catalog.dto.StockAdjustmentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/internal/products")
@Transactional(readOnly = true)
public class InternalProductController {
  private final ProductService productService;

  public InternalProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductInternalResponse> getProduct(@PathVariable Long productId) {
    return ResponseEntity.ok(
        new ProductInternalResponse(productService.findProductById(productId)));
  }

  @PatchMapping("/{productId}/stock")
  @Transactional
  public ResponseEntity<Void> adjustStock(
      @PathVariable Long productId, @RequestBody StockAdjustmentRequest request) {
    if (request.quantityDelta() == 0) {
      throw new IllegalArgumentException("Stock adjustment cannot be zero");
    }
    if (request.quantityDelta() < 0) {
      productService.decreaseStock(productId, request.sizeName(), -request.quantityDelta());
    } else {
      productService.increaseStock(productId, request.sizeName(), request.quantityDelta());
    }
    return ResponseEntity.noContent().build();
  }
}
