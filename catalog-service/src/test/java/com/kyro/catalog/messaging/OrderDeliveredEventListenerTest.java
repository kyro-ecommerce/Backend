package com.kyro.catalog.messaging;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class OrderDeliveredEventListenerTest {
  @Test
  void duplicateDeliveryDoesNotIncrementSalesTwice() {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    when(jdbc.update(startsWith("INSERT"), eq(9L))).thenReturn(1, 0);
    var listener = new OrderDeliveredEventListener(jdbc);
    var event =
        Map.<String, Object>of(
            "orderId", 9L, "items", List.of(Map.of("productId", 3L, "quantity", 2)));

    listener.handle(event);
    listener.handle(event);

    verify(jdbc, times(1)).update(startsWith("UPDATE product"), eq(2), eq(3L));
  }
}
