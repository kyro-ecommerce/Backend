package com.kyro.catalog.dto;

import com.kyro.catalog.Category;
import java.util.List;

public class CategoryDTO {
  private Long categoryId;
  private String name;
  private int level;

  private List<CategoryDTO> subCategories;

  public CategoryDTO() {}

  public CategoryDTO(Category category) {
    this.categoryId = category.getId();
    this.name = category.getName();
    this.level = category.getLevel();
    if (category.getLevel() < 2 && category.getSubCategories() != null) {
      this.subCategories = category.getSubCategories().stream().map(CategoryDTO::new).toList();
    }
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public List<CategoryDTO> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<CategoryDTO> subCategories) {
    this.subCategories = subCategories;
  }
}
