package com.kyro.order;

import com.kyro.order.dto.OrderDetailDTO;
import com.kyro.order.dto.PageResponse;
import com.kyro.order.dto.UpdateOrderStatusRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
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

  @GetMapping
  public ResponseEntity<PageResponse<OrderDetailDTO>> getAllOrders(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(required = false) String paymentStatus,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate endDate,
      @RequestParam(required = false) Integer minTotal,
      @RequestParam(required = false) Integer maxTotal,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) List<String> sort) {
    OrderFilter filter =
        OrderFilter.from(
            userId,
            search,
            status,
            paymentMethod,
            paymentStatus,
            startDate,
            endDate,
            minTotal,
            maxTotal);
    return ResponseEntity.ok(
        PageResponse.from(
            orderService
                .findOrders(filter, OrderService.orderPageable(page, size, sort))
                .map(OrderDetailDTO::new)));
  }

  @PatchMapping("/{orderId}/status")
  public ResponseEntity<Map<String, String>> updateOrderStatus(
      @PathVariable Long orderId,
      @jakarta.validation.Valid @RequestBody UpdateOrderStatusRequest request) {
    orderService.updateOrderStatus(orderId, request.status());
    return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái đơn hàng thành công"));
  }

  @DeleteMapping("/{orderId}")
  public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long orderId) {
    orderService.deleteOrder(orderId);
    return ResponseEntity.ok(Map.of("message", "Xóa đơn hàng thành công"));
  }
}
