package com.kyro.order;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/admin/analytics/orders")
public class AdminOrderAnalyticsController {
  private final OrderService orderService;

  public AdminOrderAnalyticsController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("/summary")
  public ResponseEntity<Map<String, Object>> getSummary(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    return ResponseEntity.ok(orderService.getOrderStatistics(date(startDate), date(endDate)));
  }

  @GetMapping("/daily-revenue")
  public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    return ResponseEntity.ok(orderService.getDailyRevenue(date(startDate), date(endDate)));
  }

  private static LocalDate date(String value) {
    return value == null || value.isBlank() ? null : LocalDate.parse(value);
  }
}
