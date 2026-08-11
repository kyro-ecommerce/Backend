package com.kyro.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kyro.catalog.Category;
import java.util.List;

public class CategoryDTO {
  private Long categoryId;
  private String name;
  private int level;
  private boolean isParent;
  private Long parentId;
  private long productCount;
  private List<CategoryDTO> subCategories;

  public CategoryDTO() {}

  public CategoryDTO(Category category, long productCount) {
    this.categoryId = category.getId();
    this.name = category.getName();
    this.level = category.getLevel();
    this.isParent = category.isParent();
    this.parentId =
        category.getParentCategory() == null ? null : category.getParentCategory().getId();
    this.productCount = productCount;
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

  @JsonProperty("isParent")
  public boolean isParent() {
    return isParent;
  }

  public void setParent(boolean parent) {
    isParent = parent;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public long getProductCount() {
    return productCount;
  }

  public void setProductCount(long productCount) {
    this.productCount = productCount;
  }

  public List<CategoryDTO> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<CategoryDTO> subCategories) {
    this.subCategories = subCategories;
  }
}
