package com.kyro.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
    @NotBlank(message = "Category name is required") String name, Integer level, Long parentId) {}
