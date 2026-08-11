package com.kyro.catalog;

import com.kyro.catalog.dto.CategoryDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping("/")
  public ResponseEntity<List<CategoryDTO>> getAllByParentAndSub() {
    return ResponseEntity.ok(categoryService.getCategoryTree());
  }

  @GetMapping("/all")
  public ResponseEntity<List<Category>> getAllCategories() {
    List<Category> categories = categoryService.getAllCategories();
    return ResponseEntity.ok().body(categories);
  }

  @GetMapping("/parent")
  public ResponseEntity<List<Category>> getAllParentCategories() {
    List<Category> parentCategories = categoryService.getAllParentCategories();
    return ResponseEntity.ok().body(parentCategories);
  }

  @GetMapping("/{topCategory}")
  public ResponseEntity<List<Category>> getChildTopCategories(
      @PathVariable("topCategory") String topCategory) {
    List<Category> childCategories = categoryService.getChildTopCategories(topCategory);
    return ResponseEntity.ok().body(childCategories);
  }

  @GetMapping("/categories")
  public ResponseEntity<List<CategoryDTO>> getCategories() {
    return ResponseEntity.ok(categoryService.getCategoryTree());
  }
}
