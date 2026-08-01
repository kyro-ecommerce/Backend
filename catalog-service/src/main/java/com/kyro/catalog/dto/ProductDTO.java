package com.kyro.catalog.dto;

import com.kyro.catalog.Category;
import com.kyro.catalog.Product;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDTO {
  private Long id;
  private String title;
  private String description;
  private int price;
  private int discountedPrice;
  private int quantity;
  private String brand;
  private String color;
  private String weight;
  private String dimension;
  private String batteryTtype;
  private String batteryCapacity;
  private String ramCapacity;
  private String romCapacity;
  private String screenSize;
  private String detailedReview;
  private String powerfulPerformance;
  private String connectionPort;
  private List<ProductSizeDTO> sizes;
  private List<ImageDTO> imageUrls;
  private double averageRating;
  private int numRatings;
  private String topLevelCategory;
  private String secondLevelCategory;
  private Long quantitySold;
  private Integer discountPercent;
  private LocalDateTime createdAt;

  // Constructor to map from Product entity
  public ProductDTO(Product product) {
    this.id = product.getId();
    this.title = product.getTitle();
    this.description = product.getDescription();
    this.price = product.getPrice();
    this.discountedPrice = product.getDiscountedPrice();
    this.quantity = product.getQuantity();
    this.brand = product.getBrand();
    this.color = product.getColor();
    this.weight = product.getWeight();
    this.dimension = product.getDimension();
    this.batteryTtype = product.getBatteryType();
    this.batteryCapacity = product.getBatteryCapacity();
    this.ramCapacity = product.getRamCapacity();
    this.romCapacity = product.getRomCapacity();
    this.screenSize = product.getScreenSize();
    this.detailedReview = product.getDetailedReview();
    this.powerfulPerformance = product.getPowerfulPerformance();
    this.connectionPort = product.getConnectionPort();
    this.discountPercent = product.getDiscountPersent();
    this.quantitySold = (product.getQuantitySold() != null) ? product.getQuantitySold() : 0L;
    this.createdAt =
        (product.getCreatedAt() != null) ? product.getCreatedAt() : LocalDateTime.now();

    // Extract size names from List<ProductSize>
    if (product.getSizes() != null) {
      this.sizes =
          product.getSizes().stream()
              .map(sizee -> new ProductSizeDTO(sizee))
              .collect(Collectors.toList());
    } else {
      this.sizes = Collections.emptyList();
    }

    // Map product images
    this.imageUrls = new ArrayList<>();

    if (product.getImages() != null && !product.getImages().isEmpty()) {
      this.imageUrls =
          product.getImages().stream()
              .map(image -> new ImageDTO(image))
              .collect(Collectors.toList());
    }

    if (product.getReviews() != null && !product.getReviews().isEmpty()) {
      double avg = product.getAverageRating(); // Get calculated formula value
      this.averageRating = Math.round(avg * 10.0) / 10.0; // Round value
    } else {
      this.averageRating = 0.0;
    }

    // Get ratings count
    this.numRatings = product.getNumRatings();

    // Map categories
    Category category = product.getCategory();
    if (category != null) {
      if (category.getLevel() == 1) {
        this.topLevelCategory = category.getName();
        this.secondLevelCategory = null;
      } else if (category.getLevel() == 2) {
        if (category.getParentCategory() != null) {
          this.topLevelCategory = category.getParentCategory().getName();
        } else {
          this.topLevelCategory = null;
        }
        this.secondLevelCategory = category.getName();
      } else {
        this.topLevelCategory = null;
        this.secondLevelCategory = null;
      }
    } else {
      this.topLevelCategory = null;
      this.secondLevelCategory = null;
    }
  }

  public ProductDTO() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public String getBatteryTtype() {
    return batteryTtype;
  }

  public void setBatteryTtype(String batteryTtype) {
    this.batteryTtype = batteryTtype;
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

  public List<ProductSizeDTO> getSizes() {
    return sizes;
  }

  public void setSizes(List<ProductSizeDTO> sizes) {
    this.sizes = sizes;
  }

  public List<ImageDTO> getImageUrls() {
    return imageUrls;
  }

  public void setImageUrls(List<ImageDTO> imageUrls) {
    this.imageUrls = imageUrls;
  }

  public double getAverageRating() {
    return averageRating;
  }

  public void setAverageRating(double averageRating) {
    this.averageRating = averageRating;
  }

  public int getNumRatings() {
    return numRatings;
  }

  public void setNumRatings(int numRatings) {
    this.numRatings = numRatings;
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

  public Long getQuantitySold() {
    return quantitySold;
  }

  public void setQuantitySold(Long quantitySold) {
    this.quantitySold = quantitySold;
  }

  public Integer getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(Integer discountPercent) {
    this.discountPercent = discountPercent;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
