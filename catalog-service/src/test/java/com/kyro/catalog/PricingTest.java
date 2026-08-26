package com.kyro.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PricingTest {
  @Test
  void roundsVndHalfUpAndHandlesBounds() {
    assertEquals(101, Pricing.salePrice(101, 0));
    assertEquals(0, Pricing.salePrice(101, 100));
    assertEquals(51, Pricing.salePrice(101, 50));
  }
}
