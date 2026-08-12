package com.kyro.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kyro.catalog.client.OrderClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class ProductServiceTest {

  @Test
  void productPageableMapsPublicFieldsAndAddsStableIdSort() {
    Pageable pageable =
        ProductService.productPageable(2, 20, List.of("price,asc", "createdAt,desc"), false);

    assertEquals(2, pageable.getPageNumber());
    assertEquals(
        Sort.Direction.ASC, pageable.getSort().getOrderFor("discountedPrice").getDirection());
    assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("createdAt").getDirection());
    assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("id").getDirection());
  }

  @Test
  void productPageableRejectsAdminOnlySortForCustomer() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ProductService.productPageable(0, 20, List.of("quantity,asc"), false));
  }

  @Test
  void productPageableRejectsOversizedPage() {
    assertThrows(
        IllegalArgumentException.class, () -> ProductService.productPageable(0, 101, null, true));
  }

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
