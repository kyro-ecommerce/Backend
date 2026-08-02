package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.exceptions.DomainException;
import com.kyro.order.dto.OrderDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing endpoints for customer order lifecycle operations. Reads authenticated user
 * state from gateway-injected headers.
 */
@RestController
@RequestMapping("${api.prefix}/orders")
@Transactional
public class OrderController {
  private static final Logger log = LoggerFactory.getLogger(OrderController.class);

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  /** Gets order history for the logged-in user. */
  @GetMapping("/user")
  public ResponseEntity<List<OrderDTO>> getUserOrders(@RequestHeader("X-User-Id") Long userId) {

    List<Order> orders = orderService.userOrderHistory(userId, null);
    List<OrderDTO> orderDTOs = orders.stream().map(OrderDTO::new).collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
  }

  /** Places a new order from current cart items. */
  @PostMapping({"", "/create/{addressId}"})
  public ResponseEntity<Map<String, Object>> createOrder(
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader("X-User-Email") String userEmail,
      @PathVariable(value = "addressId", required = false) Long pathAddressId,
      @RequestParam(value = "addressId", required = false) Long queryAddressId,
      @RequestParam(value = "paymentMethod", required = false) PaymentMethod queryPaymentMethod,
      @RequestBody(required = false) Map<String, Object> body) {

    Long addressId = pathAddressId != null ? pathAddressId : queryAddressId;
    PaymentMethod paymentMethod = queryPaymentMethod;

    if (body != null) {
      if (addressId == null && body.get("addressId") != null) {
        addressId = Long.valueOf(body.get("addressId").toString());
      }
      if (paymentMethod == null && body.get("paymentMethod") != null) {
        paymentMethod = PaymentMethod.valueOf(body.get("paymentMethod").toString().toUpperCase());
      }
    }

    if (addressId == null) {
      throw new DomainException(
          HttpStatus.BAD_REQUEST, "Address ID is required to create an order.");
    }

    List<Order> orders = orderService.placeOrder(addressId, userId, userEmail, paymentMethod);

    List<OrderDTO> orderDTOs = orders.stream().map(OrderDTO::new).collect(Collectors.toList());

    Map<String, Object> response = new HashMap<>();
    response.put("orders", orderDTOs);
    response.put("totalOrdersCreated", orders.size());
    response.put(
        "totalAmountForAllOrders",
        orders.stream()
            .mapToInt(
                order ->
                    order.getTotalDiscountedPrice() != null ? order.getTotalDiscountedPrice() : 0)
            .sum());
    response.put("message", "Đã tạo thành công " + orders.size() + " đơn hàng.");

    log.info("Successfully created {} order(s) for user ID {}", orders.size(), userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Finds an order by its ID. */
  @GetMapping("/{id}")
  public ResponseEntity<OrderDTO> findOrderById(@PathVariable("id") Long orderId) {
    Order order = orderService.findOrderById(orderId);
    OrderDTO orderDTO = new OrderDTO(order);
    return new ResponseEntity<>(orderDTO, HttpStatus.OK);
  }

  /** Gets pending orders for the logged-in user. */
  @GetMapping("/pending")
  public ResponseEntity<List<OrderDTO>> getPendingOrders(@RequestHeader("X-User-Id") Long userId) {
    List<Order> orders = orderService.userOrderHistory(userId, OrderStatus.PENDING);
    List<OrderDTO> orderDTOs = orders.stream().map(OrderDTO::new).collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
  }

  /** Gets confirmed orders for the logged-in user. */
  @GetMapping("/confirmed")
  public ResponseEntity<List<OrderDTO>> getConfirmedOrders(
      @RequestHeader("X-User-Id") Long userId) {
    List<Order> orders = orderService.userOrderHistory(userId, OrderStatus.CONFIRMED);
    List<OrderDTO> orderDTOs = orders.stream().map(OrderDTO::new).collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
  }

  /** Gets shipped orders for the logged-in user. */
  @GetMapping("/shipped")
  public ResponseEntity<List<OrderDTO>> getShippedOrders(@RequestHeader("X-User-Id") Long userId) {
    List<Order> orders = orderService.userOrderHistory(userId, OrderStatus.SHIPPED);
    List<OrderDTO> orderDTOs = orders.stream().map(OrderDTO::new).collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
  }

  /** Gets delivered orders for the logged-in user. */
  @GetMapping("/delivered")
  public ResponseEntity<List<OrderDTO>> getDeliveredOrders(
      @RequestHeader("X-User-Id") Long userId) {
    List<Order> orders = orderService.userOrderHistory(userId, OrderStatus.DELIVERED);
    List<OrderDTO> orderDTOs = orders.stream().map(OrderDTO::new).collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
  }

  /** Gets cancelled orders for the logged-in user. */
  @GetMapping("/cancelled")
  public ResponseEntity<List<OrderDTO>> getCancelledOrders(
      @RequestHeader("X-User-Id") Long userId) {
    List<Order> orders = orderService.userOrderHistory(userId, OrderStatus.CANCELLED);
    List<OrderDTO> orderDTOs = orders.stream().map(OrderDTO::new).collect(Collectors.toList());
    return ResponseEntity.ok(orderDTOs);
  }

  /** Cancels an order. */
  @PutMapping({"/cancel/{id}", "/{id}/cancel"})
  public ResponseEntity<OrderDTO> cancelOrder(
      @PathVariable("id") Long orderId, @RequestHeader("X-User-Id") Long userId) {

    Order order = orderService.findOrderById(orderId);
    if (!order.getUserId().equals(userId)) {
      throw new DomainException(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy đơn hàng này.");
    }

    Order cancelledOrder = orderService.cancelOrder(orderId);
    OrderDTO orderDTO = new OrderDTO(cancelledOrder);
    return ResponseEntity.ok(orderDTO);
  }

  /**
   * Internal endpoint queried by catalog-service to check if product has been purchased and
   * delivered.
   */
  @GetMapping("/verify-purchase")
  public boolean hasPurchasedAndDelivered(
      @RequestParam("userId") Long userId, @RequestParam("productId") Long productId) {

    List<Order> orders = orderService.userOrderHistory(userId, OrderStatus.DELIVERED);
    return orders.stream()
        .flatMap(o -> o.getOrderItems().stream())
        .anyMatch(oi -> oi.getProductId().equals(productId));
  }

  /**
   * Internal endpoint queried by payment-service to update order payment status on VNPay callback.
   */
  @PutMapping("/{id}/payment-status")
  public ResponseEntity<Void> updatePaymentStatus(
      @PathVariable("id") Long orderId, @RequestParam("status") PaymentStatus status) {
    orderService.updatePaymentStatus(orderId, status);
    return ResponseEntity.ok().build();
  }
}
