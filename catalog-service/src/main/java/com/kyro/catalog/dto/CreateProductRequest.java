package com.kyro.catalog.dto;

import com.kyro.catalog.ProductAttribute;
import com.kyro.catalog.ProductVariant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateProductRequest(
    @NotBlank @Size(max = 100) String title,
    @Size(max = 500) String description,
    String detailedReview,
    @NotBlank @Size(max = 50) String brand,
    @Min(0) @Max(100) int discountPercent,
    @NotBlank String topLevelCategory,
    @NotBlank String secondLevelCategory,
    @NotEmpty List<@Valid ProductVariant> variants,
    List<@Valid ProductAttribute> attributes) {}
