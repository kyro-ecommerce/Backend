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

  @GetMapping
  public ResponseEntity<List<CategoryDTO>> getAllByParentAndSub() {
    return ResponseEntity.ok(categoryService.getCategoryTree());
  }
}
