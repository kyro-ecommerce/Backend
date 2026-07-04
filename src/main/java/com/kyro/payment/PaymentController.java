package com.kyro.payment;

import com.kyro.auth.User;
import com.kyro.auth.UserService;
import com.kyro.exceptions.DomainException;
import com.kyro.order.Order;
import com.kyro.order.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     * @param jwt JWT token cho xác thực
     * @param orderId ID của đơn hàng cần thanh toán
     * @return URL thanh toán
     */
    @PostMapping("/create/{orderId}")
    public ResponseEntity<Map<String, Object>> createPayment(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId) {
        
        // Kiểm tra người dùng và quyền
        User user = userService.findUserByJwt(jwt);
        Order order = orderService.findOrderById(orderId);

        // Kiểm tra đơn hàng thuộc về người dùng
        if (!order.getUser().getId().equals(user.getId())) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập đơn hàng này");
        }

        // Tạo URL thanh toán
        String paymentUrl = paymentService.createPayment(orderId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo URL thanh toán thành công",
                "paymentUrl", paymentUrl
        ));
    }

    /**
     * Xử lý kết quả thanh toán từ VNPay - hỗ trợ cả GET và POST
     * @param params Các tham số nhận được từ VNPay
     * @return Thông tin kết quả thanh toán
     */
    @GetMapping("/vnpay-callback")
    public ResponseEntity<Map<String, Object>> vnpayCallbackPost(@RequestParam Map<String, String> params) {
        // Kiểm tra xem có vnp_TxnRef không trước khi xử lý
        if (params.get("vnp_TxnRef") == null || params.get("vnp_TxnRef").isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã giao dịch vnp_TxnRef");
        }

        PaymentDetail payment = paymentService.processPaymentCallback(params);

        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        if (vnp_ResponseCode == null) {
            vnp_ResponseCode = params.get("vnp_TransactionStatus"); // Backup option
        }

        Map<String, Object> response = new HashMap<>();

        if ("00".equals(vnp_ResponseCode)) {
            response.put("success", true);
            response.put("message", "Thanh toán thành công");
            response.put("orderId", payment.getOrder().getId());
            response.put("paymentId", payment.getId());
            response.put("transactionId", payment.getTransactionId());
        } else {
            response.put("success", false);
            response.put("message", "Thanh toán thất bại");
            response.put("responseCode", vnp_ResponseCode);
            response.put("orderId", payment.getOrder().getId());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin thanh toán theo ID đơn hàng
     * @param jwt JWT token cho xác thực
     * @param orderId ID của đơn hàng
     * @return Thông tin thanh toán
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentDetail> getPaymentByOrderId(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long orderId) {
        
        // Kiểm tra người dùng và quyền
        User user = userService.findUserByJwt(jwt);
        Order order = orderService.findOrderById(orderId);

        // Kiểm tra đơn hàng thuộc về người dùng
        if (!order.getUser().getId().equals(user.getId())) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập đơn hàng này");
        }

        // Lấy thông tin thanh toán
        PaymentDetail payment = order.getPaymentDetails();
        if (payment == null) {
            throw new DomainException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin thanh toán");
        }

        return ResponseEntity.ok(payment);
    }
}