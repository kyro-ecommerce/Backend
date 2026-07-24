package com.kyro.catalog;

import com.kyro.catalog.dto.CreateProductRequest;
import com.kyro.catalog.dto.ProductDTO;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/products")
public class AdminProductController {

  private final ProductService productService;

  @PostMapping("/create")
  public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest request) {
    Product product = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(product);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
    return ResponseEntity.ok(new ProductDTO(productService.findProductById(productId)));
  }

  @DeleteMapping("/{productId}/delete")
  public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long productId) {
    productService.adminDeleteProduct(productId);
    return ResponseEntity.ok(
        Map.of("message", String.format("Delete product have ID %d successfully", productId)));
  }

  @GetMapping("/all")
  public ResponseEntity<Page<ProductDTO>> findAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String topLevelCategory,
      @RequestParam(required = false) String secondLevelCategory,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) Integer minPrice,
      @RequestParam(required = false) Integer maxPrice,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String status) {
    Sort sortObj = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(page, size, sortObj);

    FilterProduct filter = new FilterProduct();
    filter.setKeyword(keyword);
    filter.setTopLevelCategory(topLevelCategory);
    filter.setSecondLevelCategory(secondLevelCategory);
    filter.setColor(color);
    filter.setMinPrice(minPrice);
    filter.setMaxPrice(maxPrice);
    filter.setSort(sort);

    Page<ProductDTO> productPage = productService.getProductsWithFilter(pageable, filter, status);
    return ResponseEntity.ok(productPage);
  }

  @PutMapping("/{productId}/update")
  public ResponseEntity<ProductDTO> updateProduct(
      @PathVariable Long productId, @RequestBody Product product) {
    ProductDTO updatedProduct = productService.updateProductByID(productId, product);
    return ResponseEntity.ok(updatedProduct);
  }

  @PostMapping("/create-multiple")
  public ResponseEntity<Map<String, String>> createMultipleProducts(
      @RequestBody CreateProductRequest[] requests) {
    for (CreateProductRequest request : requests) {
      productService.createProduct(request);
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("message", "Tạo nhiều sản phẩm thành công"));
  }

  @GetMapping("/top-selling")
  public ResponseEntity<List<Map<String, Object>>> getTopSellingProducts(
      @RequestParam(defaultValue = "10") int limit) {
    List<Map<String, Object>> topProducts = productService.getTopSellingProducts(limit);
    return ResponseEntity.ok(topProducts);
  }

  @GetMapping("/revenue-by-category")
  public ResponseEntity<Map<String, Object>> getRevenueByCateogry() {
    Map<String, Object> categoryRevenue = productService.getRevenueByCateogry();
    return ResponseEntity.ok(categoryRevenue);
  }

  @GetMapping("/filter-stats")
  public ResponseEntity<Map<String, Object>> getFilterStatistics() {
    Map<String, Object> stats = productService.getAdminFilterStatistics();
    return ResponseEntity.ok(stats);
  }

  @GetMapping("/categories")
  public ResponseEntity<Map<String, Object>> getAllCategories() {
    Map<String, Object> categories = productService.getAllCategories();
    return ResponseEntity.ok(categories);
  }
}
