package com.kyro.catalog.dto;

import com.kyro.catalog.Image;
import com.kyro.catalog.ProductSize;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class CreateProductRequest {
  @NotBlank(message = "Title is required")
  @Size(max = 100, message = "Title must be less than 100 characters")
  private String title;

  @Size(max = 500, message = "Description must be less than 500 characters")
  private String description;

  @NotNull(message = "Price is required")
  @Min(value = 0, message = "Price must be greater than or equal to 0")
  private int price;

  @Min(value = 0, message = "Discount percent must be between 0 and 100")
  @Max(value = 100, message = "Discount percent must be between 0 and 100")
  private int discountPersent;

  @Min(value = 0, message = "Discounted price must be greater than or equal to 0")
  private int discountedPrice;

  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity must be greater than or equal to 0")
  private int quantity;

  @NotBlank(message = "Brand is required")
  @Size(max = 50, message = "Brand must be less than 50 characters")
  private String brand;

  @Size(max = 20, message = "Color must be less than 20 characters")
  private String color;

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

  private List<ProductSize> sizes = new ArrayList<>();

  @Size(max = 255, message = "Image URL must be less than 255 characters")
  private List<Image> imageUrls = new ArrayList<>();

  @NotBlank(message = "Top level category is required")
  private String topLevelCategory;

  @NotBlank(message = "Second level category is required")
  private String secondLevelCategory;

  public CreateProductRequest() {}

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

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getDiscountPersent() {
    return discountPersent;
  }

  public void setDiscountPersent(int discountPersent) {
    this.discountPersent = discountPersent;
  }

  public int getDiscountedPrice() {
    return discountedPrice;
  }

  public void setDiscountedPrice(int discountedPrice) {
    this.discountedPrice = discountedPrice;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
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

  public List<ProductSize> getSizes() {
    return sizes;
  }

  public void setSizes(List<ProductSize> sizes) {
    this.sizes = sizes;
  }

  public List<Image> getImageUrls() {
    return imageUrls;
  }

  public void setImageUrls(List<Image> imageUrls) {
    this.imageUrls = imageUrls;
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
}
