package com.kyro.catalog;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Pricing {
  private Pricing() {}

  public static long salePrice(long price, int discountPercent) {
    return BigDecimal.valueOf(price)
        .multiply(BigDecimal.valueOf(100L - discountPercent))
        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
        .longValueExact();
  }
}
