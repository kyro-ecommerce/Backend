package com.kyro.catalog.dto;

import com.kyro.catalog.Category;
import com.kyro.catalog.Product;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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



    // Constructor để chuyển đổi từ Product entity
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
        this.createdAt = (product.getCreatedAt() != null) ? product.getCreatedAt() : LocalDateTime.now();

        // Lấy danh sách tên size từ List<ProductSize>
        if (product.getSizes() != null) {
            this.sizes = product.getSizes().stream()
                    .map(sizee -> new ProductSizeDTO(sizee))
                    .collect(Collectors.toList());
        } else {
            this.sizes = Collections.emptyList();
        }

        // Xử lý danh sách hình ảnh
        this.imageUrls = new ArrayList<>();

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            this.imageUrls = product.getImages().stream()
                    .map(image -> new ImageDTO(image))
                    .collect(Collectors.toList());
        }

        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            double avg = product.getAverageRating(); // Lấy giá trị đã tính bằng @Formula
            this.averageRating = Math.round(avg * 10.0) / 10.0; // Làm tròn ở đây
        } else {
            this.averageRating = 0.0;
        }

        // Lấy số lượng đánh giá
        this.numRatings = product.getNumRatings();


        // Xử lý categories
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
}