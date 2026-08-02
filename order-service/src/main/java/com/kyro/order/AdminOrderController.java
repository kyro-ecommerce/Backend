package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.order.dto.OrderDetailDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin/orders")
@Transactional
public class AdminOrderController {

  private final OrderService orderService;

  public AdminOrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping({"", "/all"})
  public ResponseEntity<Page<OrderDetailDTO>> getAllOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    Pageable pageable = PageRequest.of(page, size);
    Page<OrderDetailDTO> ordersPage =
        orderService.getAllOrdersWithFilters(
            search,
            status,
            startDate != null ? LocalDate.parse(startDate) : null,
            endDate != null ? LocalDate.parse(endDate) : null,
            pageable);

    return ResponseEntity.ok(ordersPage);
  }

  @PutMapping("/{orderId}/confirm")
  public ResponseEntity<Map<String, String>> confirmOrder(@PathVariable Long orderId) {
    orderService.confirmedOrder(orderId);
    return ResponseEntity.ok(Map.of("message", "Xác nhận đơn hàng thành công"));
  }

  @PutMapping("/{orderId}/ship")
  public ResponseEntity<Map<String, String>> shipOrder(@PathVariable Long orderId) {
    orderService.shippedOrder(orderId);
    return ResponseEntity.ok(Map.of("message", "Chuyển trạng thái vận chuyển thành công"));
  }

  @PutMapping("/{orderId}/deliver")
  public ResponseEntity<Map<String, String>> deliverOrder(@PathVariable Long orderId) {
    orderService.deliveredOrder(orderId);
    return ResponseEntity.ok(Map.of("message", "Đánh dấu đã giao hàng thành công"));
  }

  @PutMapping("/{orderId}/cancel")
  public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long orderId) {
    orderService.cancelOrder(orderId);
    return ResponseEntity.ok(Map.of("message", "Hủy đơn hàng thành công"));
  }

  @PutMapping("/{orderId}/status")
  public ResponseEntity<Map<String, String>> updateOrderStatus(
      @PathVariable Long orderId, @RequestBody Map<String, String> body) {
    String statusStr = body.get("status");
    if (statusStr == null || statusStr.trim().isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái không hợp lệ"));
    }
    OrderStatus status = OrderStatus.valueOf(statusStr.trim().toUpperCase());
    orderService.updateOrderStatus(orderId, status);
    return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái đơn hàng thành công"));
  }

  @DeleteMapping("/{orderId}")
  public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long orderId) {
    orderService.deleteOrder(orderId);
    return ResponseEntity.ok(Map.of("message", "Xóa đơn hàng thành công"));
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getOrderStats(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    LocalDate start =
        (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
    LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

    Map<String, Object> stats = orderService.getOrderStatistics(start, end);
    return ResponseEntity.ok(stats);
  }

  @GetMapping("/daily-revenue")
  public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    LocalDate start =
        (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate) : null;
    LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : null;

    List<Map<String, Object>> dailyRevenue = orderService.getDailyRevenue(start, end);
    return ResponseEntity.ok(dailyRevenue);
  }
}
