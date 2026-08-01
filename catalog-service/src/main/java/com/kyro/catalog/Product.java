package com.kyro.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Formula;

@Entity
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String color;

  @NotBlank(message = "Title is required")
  @Size(max = 100, message = "Title must be less than 100 characters")
  @Column(name = "title")
  private String title;

  @NotBlank(message = "Brand is required")
  @Size(max = 50, message = "Brand must be less than 50 characters")
  @Column(name = "brand")
  private String brand;

  @Column(name = "weight")
  private String weight;

  @Column(name = "dimension")
  private String dimension;

  @Column(name = "battery_type")
  private String batteryType;

  @Column(name = "battery_capacity")
  private String batteryCapacity;

  @Column(name = "ram_capacity")
  private String ramCapacity;

  @Column(name = "rom_capacity")
  private String romCapacity;

  @Column(name = "screen_size")
  private String screenSize;

  @Column(name = "detailed_review")
  private String detailedReview;

  @Column(name = "powerful_performance")
  private String powerfulPerformance;

  @Column(name = "connection_port")
  private String connectionPort;

  @NotNull(message = "Price is required")
  @Min(value = 0, message = "Price must be greater than or equal to 0")
  @Column(precision = 19, scale = 2)
  private int price;

  @Formula("(SELECT COALESCE(SUM(s.quantity), 0) FROM sizes s WHERE s.product_id = id)")
  private int quantity; // Calculated field mapping to product sizes stock count

  @Size(max = 500, message = "Description must be less than 500 characters")
  @Column(columnDefinition = "TEXT")
  private String description;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @Min(value = 0, message = "Discount percent must be greater than or equal to 0")
  @Max(value = 100, message = "Discount percent must be less than or equal to 100")
  @Column(name = "discount_persent")
  private int discountPersent;

  @Min(value = 0, message = "Discounted price must be greater than or equal to 0")
  @Column(name = "discounted_price")
  private int discountedPrice;

  @Size(max = 255, message = "Image URL must be less than 255 characters")
  @Column(name = "image_urls")
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Image> images = new ArrayList<>();

  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<ProductSize> sizes = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<Review> reviews = new ArrayList<>();

  @Column(name = "num_ratings", nullable = false, columnDefinition = "integer default 0")
  private int numRatings = 0;

  @Column(
      name = "average_rating",
      nullable = false,
      columnDefinition = "double precision default 0.0")
  private double averageRating = 0.0;

  @Column(name = "quantity_sold")
  private Long quantitySold;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public Product() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
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

  public int getDiscountPersent() {
    return discountPersent;
  }

  public void setDiscountPersent(int discountPersent) {
    this.discountPersent = discountPersent;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public List<ProductSize> getSizes() {
    return sizes;
  }

  public void setSizes(List<ProductSize> sizes) {
    this.sizes = sizes;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
  }

  public List<Review> getReviews() {
    return reviews;
  }

  public void setReviews(List<Review> reviews) {
    this.reviews = reviews;
  }

  public int getNumRatings() {
    return numRatings;
  }

  public void setNumRatings(int numRatings) {
    this.numRatings = numRatings;
  }

  public double getAverageRating() {
    return averageRating;
  }

  public void setAverageRating(double averageRating) {
    this.averageRating = averageRating;
  }

  public Long getQuantitySold() {
    return quantitySold;
  }

  public void setQuantitySold(Long quantitySold) {
    this.quantitySold = quantitySold;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public void updateDiscountedPrice() {
    this.discountedPrice = price - (price * discountPersent / 100);
  }
}
