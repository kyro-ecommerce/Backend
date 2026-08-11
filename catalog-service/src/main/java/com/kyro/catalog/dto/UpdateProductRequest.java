package com.kyro.catalog.dto;

import com.kyro.catalog.ProductSize;
import java.util.ArrayList;
import java.util.List;

public class UpdateProductRequest {
  private String title;

  private String description;

  private Integer price;

  private Integer discountPersent;

  private Integer quantity;

  private String brand;

  private String color;

  // Add specification fields
  private String weight;
  private String dimension;
  private String batteryType;
  private String batteryCapacity;
  private String ramCapacity;
  private String romCapacity;
  private String screenSize;
  private String detailedReview;
  private String powerfulPerformance;
  private String connectionPort;

  // Add category fields like CreateProductRequest
  private String topLevelCategory;
  private String secondLevelCategory;

  private List<ProductSize> sizes = new ArrayList<>();

  public UpdateProductRequest() {}

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getPrice() {
    return price;
  }

  public void setPrice(Integer price) {
    this.price = price;
  }

  public Integer getDiscountPersent() {
    return discountPersent;
  }

  public void setDiscountPersent(Integer discountPersent) {
    this.discountPersent = discountPersent;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getWeight() {
    return weight;
  }

  public void setWeight(String weight) {
    this.weight = weight;
  }

  public String getDimension() {
    return dimension;
  }

  public void setDimension(String dimension) {
    this.dimension = dimension;
  }

  public String getBatteryType() {
    return batteryType;
  }

  public void setBatteryType(String batteryType) {
    this.batteryType = batteryType;
  }

  public String getBatteryCapacity() {
    return batteryCapacity;
  }

  public void setBatteryCapacity(String batteryCapacity) {
    this.batteryCapacity = batteryCapacity;
  }

  public String getRamCapacity() {
    return ramCapacity;
  }

  public void setRamCapacity(String ramCapacity) {
    this.ramCapacity = ramCapacity;
  }

  public String getRomCapacity() {
    return romCapacity;
  }

  public void setRomCapacity(String romCapacity) {
    this.romCapacity = romCapacity;
  }

  public String getScreenSize() {
    return screenSize;
  }

  public void setScreenSize(String screenSize) {
    this.screenSize = screenSize;
  }

  public String getDetailedReview() {
    return detailedReview;
  }

  public void setDetailedReview(String detailedReview) {
    this.detailedReview = detailedReview;
  }

  public String getPowerfulPerformance() {
    return powerfulPerformance;
  }

  public void setPowerfulPerformance(String powerfulPerformance) {
    this.powerfulPerformance = powerfulPerformance;
  }

  public String getConnectionPort() {
    return connectionPort;
  }

  public void setConnectionPort(String connectionPort) {
    this.connectionPort = connectionPort;
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

  public List<ProductSize> getSizes() {
    return sizes;
  }

  public void setSizes(List<ProductSize> sizes) {
    this.sizes = sizes;
  }
}
