package com.kyro.catalog;

import com.kyro.catalog.dto.CategoryDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/categories")
public class CategoryController {
  private final CategoryService categoryService;

  @GetMapping("/")
  public ResponseEntity<List<CategoryDTO>> getAllByParentAndSub() {
    List<Category> categories = categoryService.getAllParentCategories();
    List<CategoryDTO> categoryDTOs =
        categories.stream().map(this::convertToDTO).collect(Collectors.toList());
    return ResponseEntity.ok(categoryDTOs);
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
    List<Category> categories = categoryService.getAllCategories();
    List<CategoryDTO> categoryDTOs = categories.stream().map(this::convertToDTO).toList();

    return ResponseEntity.ok(categoryDTOs);
  }

  private CategoryDTO convertToDTO(Category category) {
    return new CategoryDTO(category);
  }
}
