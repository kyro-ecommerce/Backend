package com.kyro.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kyro.catalog.client.OrderClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

  @Test
  void mapsDeliveredSalesInRankedOrderAndOverridesReservationCounter() {
    Product first = product(1L, "First", 99L);
    Product second = product(2L, "Second", 99L);

    List<Map<String, Object>> result =
        ProductService.mapTopSellingProducts(
            List.of(
                new OrderClient.TopSellingProductResponse(2L, 7L),
                new OrderClient.TopSellingProductResponse(1L, 4L)),
            List.of(first, second));

    assertEquals(2L, result.get(0).get("id"));
    assertEquals(7L, result.get(0).get("quantitySold"));
    assertEquals(1L, result.get(1).get("id"));
    assertEquals(4L, result.get(1).get("quantitySold"));
  }

  private Product product(Long id, String title, Long quantitySold) {
    Product product = new Product();
    product.setId(id);
    product.setTitle(title);
    product.setQuantitySold(quantitySold);
    return product;
  }
}
