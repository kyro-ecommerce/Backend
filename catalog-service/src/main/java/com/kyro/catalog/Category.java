package com.kyro.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Category {

  public Category() {}

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

  @JsonIgnore
  @OneToMany(mappedBy = "category")
  List<Product> products;

  public Category(String name) {
    this.name = name;
  }

  public Category(String name, Category parentCategory) {
    this.name = name;
    this.parentCategory = parentCategory;
  }

  public void addSubCategory(Category subCategory) {
    subCategories.add(subCategory);
    subCategory.setParentCategory(this);
  }

  public void removeSubCategory(Category subCategory) {
    subCategories.remove(subCategory);
    subCategory.setParentCategory(null);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Category getParentCategory() {
    return parentCategory;
  }

  public void setParentCategory(Category parentCategory) {
    this.parentCategory = parentCategory;
  }

  public List<Category> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<Category> subCategories) {
    this.subCategories = subCategories;
  }

  public List<Product> getProducts() {
    return products;
  }

  public void setProducts(List<Product> products) {
    this.products = products;
  }
}
