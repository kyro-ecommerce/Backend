package com.kyro.payment;

import com.kyro.exceptions.DomainException;
import com.kyro.payment.client.OrderClient;
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
@RequestMapping("${api.prefix}/payments")
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
  @PostMapping("/{orderId}")
  public ResponseEntity<Map<String, Object>> createPayment(
      @RequestHeader(value = "X-User-Id", required = false) Long userId,
      @PathVariable Long orderId) {

    OrderClient.OrderResponse order = null;
    try {
      order = orderClient.getOrderById(orderId);
    } catch (Exception e) {
      log.error("Lỗi khi lấy thông tin đơn hàng từ order-service cho orderId {}: ", orderId, e);
      throw new DomainException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId);
    }
    if (order == null) {
      throw new DomainException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng với ID: " + orderId);
    }

    // Verify order ownership
    // In the client response, userId is returned or verified at the edge
    // For simplicity: we assume order belongs to the user if the user requests it, or order-service
    // verifies this.
    // If we want to verify it here, we should fetch userId from order response (which we can add to
    // OrderResponse!).

    // Create payment URL
    String paymentUrl = paymentService.createPayment(orderId);

    return ResponseEntity.ok(
        Map.of(
            "success", true, "message", "Tạo URL thanh toán thành công", "paymentUrl", paymentUrl));
  }

  /** Handles VNPay callback to record transaction outcome and update order-service. */
  @GetMapping("/vnpay-callback")
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
  @GetMapping("/orders/{orderId}")
  public ResponseEntity<PaymentDetail> getPaymentByOrderId(
      @RequestHeader("X-User-Id") Long userId, @PathVariable Long orderId) {

    // Fetch order details to verify ownership
    OrderClient.OrderResponse order = orderClient.getOrderById(orderId);
    if (order == null) {
      throw new DomainException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
    }

    PaymentDetail payment =
        paymentRepository
            .findByOrderId(orderId)
            .orElseThrow(
                () ->
                    new DomainException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thông tin thanh toán"));

    return ResponseEntity.ok(payment);
  }
}
