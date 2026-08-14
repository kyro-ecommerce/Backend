package com.kyro.order;

import com.kyro.order.dto.OrderInternalResponse;
import com.kyro.order.dto.TopSellingProductResponse;
import com.kyro.order.dto.ProductRevenueResponse;
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
    return orderService.hasPurchasedAndDelivered(userId, productId);
  }

  @GetMapping("/top-selling")
  public List<TopSellingProductResponse> getTopSellingProducts(
      @RequestParam(defaultValue = "10") int limit) {
    return orderService.getTopSellingProducts(limit);
  }
  @GetMapping("/product-revenue")
  public List<ProductRevenueResponse> getProductRevenue() { return orderService.getProductRevenue(); }
}
