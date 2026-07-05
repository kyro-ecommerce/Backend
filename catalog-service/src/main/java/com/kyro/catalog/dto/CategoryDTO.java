package com.kyro.catalog.dto;

import com.kyro.catalog.Category;
import java.util.List;
import lombok.Data;

@Data
public class CategoryDTO {
  private Long categoryId;
  private String name;
  private int level;

  private List<CategoryDTO> subCategories;

  public CategoryDTO(Category category) {
    this.categoryId = category.getId();
    this.name = category.getName();
    this.level = category.getLevel();
    if (category.getLevel() < 2 && category.getSubCategories() != null) {
      this.subCategories = category.getSubCategories().stream().map(CategoryDTO::new).toList();
    }
  }
}
