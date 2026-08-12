package com.kyro.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.kyro.enums.OrderStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class AdminOrderControllerTest {

  @Test
  void defaultsToStableNewestFirstSort() {
    Pageable pageable = OrderService.orderPageable(0, 20, null);

    assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("orderDate").getDirection());
    assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
  }

  @Test
  void rejectsUnsupportedSort() {
    assertThrows(
        IllegalArgumentException.class,
        () -> OrderService.orderPageable(0, 20, List.of("paymentStatus,desc")));
  }

  @Test
  void parsesFiltersCaseInsensitively() {
    OrderFilter filter =
        OrderFilter.from(
            7L,
            null,
            "delivered",
            "cod",
            null,
            LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-31"),
            100,
            200);

    assertEquals(7L, filter.userId());
    assertEquals(OrderStatus.DELIVERED, filter.status());
  }
}
