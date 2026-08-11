package com.kyro.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageUrlRequest(@NotBlank(message = "Image URL is required") String url) {}
