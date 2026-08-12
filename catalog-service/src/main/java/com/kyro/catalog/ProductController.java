package com.kyro.catalog;

import com.kyro.catalog.dto.PageResponse;
import com.kyro.catalog.dto.ProductDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/products")
@Transactional(readOnly = true)
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public ResponseEntity<PageResponse<ProductDTO>> findProductsByFilter(
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String color,
      @RequestParam(required = false) Integer minPrice,
      @RequestParam(required = false) Integer maxPrice,
      @RequestParam(required = false) String keyword,
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
                ProductService.productPageable(page, size, sort, false), filter)));
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ProductDTO> findProductById(@PathVariable Long productId) {
    Product product = productService.findProductById(productId);
    ProductDTO productDTO = new ProductDTO(product);
    return ResponseEntity.ok(productDTO);
  }
}
