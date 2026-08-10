package com.kyro.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class AdminOrderControllerTest {

  @Test
  void defaultsToStableNewestFirstSort() {
    Pageable pageable = AdminOrderController.adminPageable(0, 10, "orderDate", "desc");

    assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("orderDate").getDirection());
    assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
  }

  @Test
  void rejectsUnsupportedSort() {
    assertThrows(
        IllegalArgumentException.class,
        () -> AdminOrderController.adminPageable(0, 10, "paymentStatus", "desc"));
  }
}
