package com.kyro.payment;

import com.kyro.exceptions.DomainException;
import com.kyro.payment.client.OrderClient;
import feign.FeignException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing endpoints for creating payments and handling VNPay callbacks. Reads
 * authenticated user state from gateway-injected headers.
 */
@RestController
@RequestMapping("${api.prefix}")
public class PaymentController {

  private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

  private final PaymentService paymentService;
  private final OrderClient orderClient;
  private final PaymentRepository paymentRepository;

  public PaymentController(
      PaymentService paymentService, OrderClient orderClient, PaymentRepository paymentRepository) {
    this.paymentService = paymentService;
    this.orderClient = orderClient;
    this.paymentRepository = paymentRepository;
  }

  /** Creates a VNPay checkout URL for an order. */
  @PostMapping("/orders/{orderId}/payments")
  public ResponseEntity<Map<String, Object>> createPayment(
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader(value = "X-User-Roles", required = false) String roles,
      @PathVariable Long orderId) {

    OrderClient.OrderResponse order;
    try {
      order = orderClient.getOrderById(orderId);
    } catch (FeignException e) {
      log.error("Lỗi khi lấy thông tin đơn hàng từ order-service cho orderId {}: ", orderId, e);
      if (e.status() == HttpStatus.NOT_FOUND.value()) {
        throw new DomainException(
            HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId);
      }
      throw new DomainException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "DEPENDENCY_UNAVAILABLE",
          "Không thể truy cập dịch vụ đơn hàng");
    }
    if (order == null) {
      throw new DomainException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId);
    }
    verifyOwnership(userId, roles, order);

    // Create payment URL
    String paymentUrl = paymentService.createPayment(orderId);

    return ResponseEntity.ok(
        Map.of(
            "success", true, "message", "Tạo URL thanh toán thành công", "paymentUrl", paymentUrl));
  }

  /** Handles VNPay callback to record transaction outcome and update order-service. */
  @GetMapping("/payment-providers/vnpay/callback")
  public ResponseEntity<Map<String, Object>> vnpayCallback(
      @RequestParam Map<String, String> params) {
    if (params.get("vnp_TxnRef") == null || params.get("vnp_TxnRef").isEmpty()) {
      throw new IllegalArgumentException("Thiếu mã giao dịch vnp_TxnRef");
    }

    PaymentDetail payment = paymentService.processPaymentCallback(params);

    String vnp_ResponseCode = params.get("vnp_ResponseCode");
    if (vnp_ResponseCode == null) {
      vnp_ResponseCode = params.get("vnp_TransactionStatus");
    }

    Map<String, Object> response = new HashMap<>();

    if ("00".equals(vnp_ResponseCode)) {
      response.put("success", true);
      response.put("message", "Thanh toán thành công");
      response.put("orderId", payment.getOrderId());
      response.put("paymentId", payment.getId());
      response.put("transactionId", payment.getTransactionId());
    } else {
      response.put("success", false);
      response.put("message", "Payment failed");
      response.put("responseCode", vnp_ResponseCode);
      response.put("orderId", payment.getOrderId());
    }

    return ResponseEntity.ok(response);
  }

  /** Gets payment details by order ID. */
  @GetMapping("/orders/{orderId}/payment")
  public ResponseEntity<PaymentDetail> getPaymentByOrderId(
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader(value = "X-User-Roles", required = false) String roles,
      @PathVariable Long orderId) {

    // Fetch order details to verify ownership
    OrderClient.OrderResponse order = orderClient.getOrderById(orderId);
    if (order == null) {
      throw new DomainException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
    }
    verifyOwnership(userId, roles, order);

    PaymentDetail payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () ->
                    new DomainException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin thanh toán"));

    return ResponseEntity.ok(payment);
  }

  private static void verifyOwnership(Long userId, String roles, OrderClient.OrderResponse order) {
    boolean admin =
        roles != null
            && Arrays.stream(roles.split(","))
                .anyMatch(role -> "ADMIN".equals(role) || "ROLE_ADMIN".equals(role));
    if (!admin && !userId.equals(order.userId())) {
      throw new DomainException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập đơn hàng này");
    }
  }
}
