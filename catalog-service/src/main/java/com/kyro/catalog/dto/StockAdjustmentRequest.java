package com.kyro.catalog.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(@NotNull Long variantId, int quantityDelta) {}
