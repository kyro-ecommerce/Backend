package com.kyro.catalog;

import com.kyro.catalog.dto.CreateProductRequest;
import com.kyro.catalog.dto.PageResponse;
import com.kyro.catalog.dto.ProductDTO;
import com.kyro.catalog.dto.UpdateProductRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin/products")
public class AdminProductController {

  private final ProductService productService;

  public AdminProductController(ProductService productService) {
    this.productService = productService;
  }

  @PostMapping
  public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
    Product product = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(product);
  }

  @GetMapping("/{productId}")
  @Transactional(readOnly = true)
  public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
    return ResponseEntity.ok(new ProductDTO(productService.findProductById(productId)));
  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long productId) {
    productService.adminDeleteProduct(productId);
    return ResponseEntity.ok(
        Map.of("message", String.format("Delete product have ID %d successfully", productId)));
  }

  @GetMapping
  public ResponseEntity<PageResponse<ProductDTO>> findAllProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) Integer minPrice,
      @RequestParam(required = false) Integer maxPrice,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) Boolean inStock,
      @RequestParam(required = false) Double minRating,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) List<String> sort) {
    FilterProduct filter =
        new FilterProduct(
            categoryId, color, minPrice, maxPrice, keyword, brand, inStock, minRating);
    return ResponseEntity.ok(
        PageResponse.from(
            productService.getProductsWithFilter(
                ProductService.productPageable(page, size, sort, true), filter, true)));
  }

  @PatchMapping("/{productId}")
  public ResponseEntity<ProductDTO> updateProduct(
      @PathVariable Long productId, @Valid @RequestBody UpdateProductRequest product) {
    ProductDTO updatedProduct = productService.updateProductByID(productId, product);
    return ResponseEntity.ok(updatedProduct);
  }

  @PostMapping("/product-imports")
  public ResponseEntity<Map<String, String>> createMultipleProducts(
      @Valid @RequestBody CreateProductRequest[] requests) {
    for (CreateProductRequest request : requests) {
      productService.createProduct(request);
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("message", "Tạo nhiều sản phẩm thành công"));
  }
}
