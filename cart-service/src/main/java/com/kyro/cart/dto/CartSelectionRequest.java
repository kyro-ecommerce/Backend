package com.kyro.cart.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record CartSelectionRequest(@NotEmpty List<@NotNull @Positive Long> cartItemIds) {}
