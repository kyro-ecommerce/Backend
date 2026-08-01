package com.kyro.catalog;

public class FilterProduct {
  private String topLevelCategory;
  private String secondLevelCategory;
  private String color;
  private Integer minPrice;
  private Integer maxPrice;
  private String sort;
  private String keyword;
  private String brand;

  public FilterProduct() {}

  public FilterProduct(
      String topLevelCategory,
      String secondLevelCategory,
      String color,
      Integer minPrice,
      Integer maxPrice,
      String sort,
      String keyword,
      String brand) {
    this.topLevelCategory = topLevelCategory;
    this.secondLevelCategory = secondLevelCategory;
    this.color = color;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.sort = sort;
    this.keyword = keyword;
    this.brand = brand;
  }

  public String getTopLevelCategory() {
    return topLevelCategory;
  }

  public void setTopLevelCategory(String topLevelCategory) {
    this.topLevelCategory = topLevelCategory;
  }

  public String getSecondLevelCategory() {
    return secondLevelCategory;
  }

  public void setSecondLevelCategory(String secondLevelCategory) {
    this.secondLevelCategory = secondLevelCategory;
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

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
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
}
