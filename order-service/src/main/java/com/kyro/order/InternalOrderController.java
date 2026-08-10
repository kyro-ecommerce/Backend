package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.dto.OrderInternalResponse;
import com.kyro.order.dto.PaymentStatusUpdateRequest;
import com.kyro.order.dto.TopSellingProductResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/internal/orders")
public class InternalOrderController {
  private final OrderService orderService;

  public InternalOrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderInternalResponse> getOrder(@PathVariable Long orderId) {
    return ResponseEntity.ok(new OrderInternalResponse(orderService.findOrderById(orderId)));
  }

  @GetMapping("/purchases")
  public boolean hasPurchasedAndDelivered(@RequestParam Long userId, @RequestParam Long productId) {
    return orderService.userOrderHistory(userId, OrderStatus.DELIVERED).stream()
        .flatMap(order -> order.getOrderItems().stream())
        .anyMatch(item -> item.getProductId().equals(productId));
  }

  @GetMapping("/top-selling")
  public List<TopSellingProductResponse> getTopSellingProducts(
      @RequestParam(defaultValue = "10") int limit) {
    return orderService.getTopSellingProducts(limit);
  }

  @PatchMapping("/{orderId}/payment-status")
  public ResponseEntity<Void> updatePaymentStatus(
      @PathVariable Long orderId, @RequestBody PaymentStatusUpdateRequest request) {
    PaymentStatus status = request.status();
    if (status == null) {
      throw new IllegalArgumentException("Payment status is required");
    }
    orderService.updatePaymentStatus(orderId, status);
    return ResponseEntity.noContent().build();
  }
}
