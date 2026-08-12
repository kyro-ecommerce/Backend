package com.kyro.catalog;

public class FilterProduct {
  private Long categoryId;
  private String color;
  private Integer minPrice;
  private Integer maxPrice;
  private String keyword;
  private String brand;
  private Boolean inStock;
  private Double minRating;

  public FilterProduct() {}

  public FilterProduct(
      Long categoryId,
      String color,
      Integer minPrice,
      Integer maxPrice,
      String keyword,
      String brand,
      Boolean inStock,
      Double minRating) {
    this.categoryId = categoryId;
    this.color = color;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.keyword = keyword;
    this.brand = brand;
    this.inStock = inStock;
    this.minRating = minRating;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public Integer getMinPrice() {
    return minPrice;
  }

  public void setMinPrice(Integer minPrice) {
    this.minPrice = minPrice;
  }

  public Integer getMaxPrice() {
    return maxPrice;
  }

  public void setMaxPrice(Integer maxPrice) {
    this.maxPrice = maxPrice;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public Boolean getInStock() {
    return inStock;
  }

  public void setInStock(Boolean inStock) {
    this.inStock = inStock;
  }

  public Double getMinRating() {
    return minRating;
  }

  public void setMinRating(Double minRating) {
    this.minRating = minRating;
  }
}
