package com.kyro.catalog.dto;

import com.kyro.catalog.ProductAttribute;
import com.kyro.catalog.ProductVariant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record UpdateProductRequest(
    @Size(max = 100) String title,
    @Size(max = 500) String description,
    String detailedReview,
    @Size(max = 50) String brand,
    @Min(0) @Max(100) Integer discountPercent,
    String topLevelCategory,
    String secondLevelCategory,
    List<@Valid ProductVariant> variants,
    List<@Valid ProductAttribute> attributes) {}
