package com.kyro.payment;

import com.google.gson.Gson;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.payment.client.OrderClient;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service for VNPay payment gateway integration. Communicates with order-service using Feign
 * clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  @Value("${vnpay.tmnCode}")
  private String vnp_TmnCode;

  @Value("${vnpay.hashSecret}")
  private String vnp_HashSecret;

  @Value("${vnpay.url}")
  private String vnp_PayUrl;

  @Value("${vnpay.returnUrl}")
  private String vnp_Returnurl;

  private final OrderClient orderClient;
  private final PaymentRepository paymentRepository;

  @Transactional
  public String createPayment(Long orderId) {
    try {
      // Get order info from order-service via FeignClient
      OrderClient.OrderResponse order = orderClient.getOrderById(orderId);
      if (order == null) {
        throw new RuntimeException("Không tìm thấy đơn hàng");
      }

      // Update payment method to VNPAY inside order-service
      // (Standard payment flow updates payment method locally or during checkout)

      String vnp_TxnRef = orderId + "_" + getRandomNumber(8);
      String vnp_OrderInfo = "Thanh toan don hang #" + orderId;
      String vnp_OrderType = "other";
      String vnp_IpAddr = getIpAddress();
      int totalAmount = order.getTotalDiscountedPrice() != null ? order.getTotalDiscountedPrice() : 0;
      long amount = (long) totalAmount * 100L;

      Map<String, String> vnp_Params = new HashMap<>();
      vnp_Params.put("vnp_Version", "2.1.0");
      vnp_Params.put("vnp_Command", "pay");
      vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
      vnp_Params.put("vnp_Amount", String.valueOf(amount));
      vnp_Params.put("vnp_CurrCode", "VND");
      vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
      vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
      vnp_Params.put("vnp_OrderType", vnp_OrderType);
      vnp_Params.put("vnp_Locale", "vn");
      vnp_Params.put("vnp_ReturnUrl", vnp_Returnurl);
      vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

      ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
      LocalDateTime now = LocalDateTime.now(vietnamZoneId);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

      String vnp_CreateDate = now.format(formatter);
      String vnp_ExpireDate = now.plusMinutes(15).format(formatter);

      vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
      vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

      // Find existing PaymentDetail or create a new record to avoid unique order_id constraint violation
      PaymentDetail paymentDetail =
          paymentRepository.findByOrderId(orderId).orElseGet(PaymentDetail::new);

      paymentDetail.setOrderId(orderId);
      paymentDetail.setPaymentMethod(PaymentMethod.VNPAY);
      paymentDetail.setPaymentStatus(PaymentStatus.PENDING);
      paymentDetail.setTotalAmount(totalAmount);
      paymentDetail.setTransactionId(vnp_TxnRef);
      if (paymentDetail.getCreatedAt() == null) {
        paymentDetail.setCreatedAt(LocalDateTime.now());
      }
      paymentDetail.setUpdatedAt(LocalDateTime.now());

      List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
      Collections.sort(fieldNames);
      StringBuilder hashData = new StringBuilder();
      StringBuilder query = new StringBuilder();

      for (String fieldName : fieldNames) {
        String fieldValue = vnp_Params.get(fieldName);
        if ((fieldValue != null) && (fieldValue.length() > 0)) {
          hashData.append(fieldName);
          hashData.append('=');
          hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

          query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
          query.append('=');
          query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

          if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
            query.append('&');
            hashData.append('&');
          }
        }
      }

      String queryUrl = query.toString();
      String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
      queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

      paymentDetail.setVnp_SecureHash(vnp_SecureHash);
      paymentRepository.save(paymentDetail);

      return vnp_PayUrl + "?" + queryUrl;
    } catch (Exception e) {
      log.error("Failed to initiate VNPay payment request: {}", e.getMessage(), e);
      throw new RuntimeException("Lỗi khi tạo yêu cầu thanh toán: " + e.getMessage());
    }
  }

  public PaymentDetail getPaymentById(Long paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(
            () -> new RuntimeException("Không tìm thấy thông tin thanh toán: " + paymentId));
  }

  @Transactional
  public PaymentDetail processPaymentCallback(Map<String, String> vnpParams) {
    try {
      String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
      String vnp_TxnRef = vnpParams.get("vnp_TxnRef");

      PaymentDetail payment =
          paymentRepository
              .findByTransactionId(vnp_TxnRef)
              .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch: " + vnp_TxnRef));

      if ("00".equals(vnp_ResponseCode)) {
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentLog(new Gson().toJson(vnpParams));
        payment.setVnp_ResponseCode(vnp_ResponseCode);

        // Update payment status in order-service via FeignClient
        orderClient.updatePaymentStatus(payment.getOrderId(), "COMPLETED");

        return paymentRepository.save(payment);
      } else {
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setPaymentLog(new Gson().toJson(vnpParams));

        // Update payment status in order-service via FeignClient
        orderClient.updatePaymentStatus(payment.getOrderId(), "FAILED");

        return paymentRepository.save(payment);
      }
    } catch (Exception e) {
      log.error("Failed to process payment callback: {}", e.getMessage(), e);
      throw new RuntimeException("Lỗi xử lý callback thanh toán: " + e.getMessage());
    }
  }

  private String getRandomNumber(int len) {
    Random rnd = new Random();
    String chars = "0123456789";
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(chars.charAt(rnd.nextInt(chars.length())));
    }
    return sb.toString();
  }

  private String getIpAddress() {
    try {
      HttpServletRequest request =
          ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
      String ipAddress = request.getHeader("X-FORWARDED-FOR");
      if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
        ipAddress = request.getRemoteAddr();
      }
      if (ipAddress != null && ipAddress.contains(",")) {
        ipAddress = ipAddress.split(",")[0].trim();
      }
      if (ipAddress == null || ipAddress.isEmpty() || "0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
        ipAddress = "127.0.0.1";
      }
      return ipAddress;
    } catch (Exception e) {
      return "127.0.0.1";
    }
  }

  private String hmacSHA512(String key, String data) {
    try {
      Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
      SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA512");
      sha512_HMAC.init(secret_key);
      byte[] hash = sha512_HMAC.doFinal(data.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception ex) {
      return "";
    }
  }
}
