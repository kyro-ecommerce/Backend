
package com.kyro.catalog.dto;

import com.kyro.catalog.Image;
import com.kyro.catalog.ProductSize;



import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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

}