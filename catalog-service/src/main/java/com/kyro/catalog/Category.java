package com.kyro.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Category name is required")
  @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters")
  @Column(unique = true)
  private String name;

  @ManyToOne
  @JoinColumn(name = "parent_category_id")
  @JsonIgnore
  private Category parentCategory;

  @JsonIgnore
  @OneToMany(
      mappedBy = "parentCategory",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<Category> subCategories = new ArrayList<>();

  // Flag to determine if this is a level 1 or level 2 category
  @Column(name = "is_parent", nullable = false)
  private boolean isParent = true;

  @JsonIgnore
  @OneToMany(mappedBy = "category")
  List<Product> products;

  public Category(String name) {
    this.name = name;
    this.isParent = true;
    this.level = 1;
  }

  public Category(String name, Category parentCategory) {
    if (parentCategory != null && parentCategory.getLevel() != 1) {
      throw new IllegalArgumentException("Parent category must be level 1.");
    }
    this.name = name;
    this.parentCategory = parentCategory;
    this.isParent = false;
    this.level = 2;
  }

  // Limit level to 1 or 2 only
  @Column(name = "level", nullable = false)
  private int level;

  public void addSubCategory(Category subCategory) {
    // Allow subcategories only if the current category is level 1
    if (this.level == 1) {
      subCategories.add(subCategory);
      subCategory.setParentCategory(this);
      subCategory.setLevel(2);
      subCategory.setParent(false);
    }
  }

  public void removeSubCategory(Category subCategory) {
    subCategories.remove(subCategory);
    subCategory.setParentCategory(null);
  }
}
