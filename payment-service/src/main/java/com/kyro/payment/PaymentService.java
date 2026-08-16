package com.kyro.payment;

import com.google.gson.Gson;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.exceptions.DomainException;
import com.kyro.payment.client.OrderClient;
import com.kyro.payment.event.PaymentStatusChangedEvent;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service for VNPay payment gateway integration. Communicates with order-service using Feign
 * clients.
 */
@Service
public class PaymentService {

  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
  private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
  private static final DateTimeFormatter VNPAY_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(VIETNAM_ZONE);

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
  private final ApplicationEventPublisher eventPublisher;

  public PaymentService(
      OrderClient orderClient,
      PaymentRepository paymentRepository,
      ApplicationEventPublisher eventPublisher) {
    this.orderClient = orderClient;
    this.paymentRepository = paymentRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public String createPayment(Long orderId) {
    try {
      // Get order info from order-service via FeignClient
      OrderClient.OrderResponse order = orderClient.getOrderById(orderId);
      if (order == null) {
        throw new DomainException(
            HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng");
      }
      if (!"VNPAY".equals(order.paymentMethod())) {
        throw new DomainException(
            HttpStatus.CONFLICT,
            "INVALID_PAYMENT_METHOD",
            "Đơn hàng không sử dụng phương thức thanh toán VNPAY");
      }
      if (!"PENDING".equals(order.orderStatus()) || "COMPLETED".equals(order.paymentStatus())) {
        throw new DomainException(
            HttpStatus.CONFLICT,
            "INVALID_PAYMENT_STATE",
            "Đơn hàng không còn ở trạng thái có thể thanh toán");
      }
      Instant now = Instant.now();
      if (isExpired(order.expiresAt(), now)) {
        throw new DomainException(
            HttpStatus.CONFLICT, "ORDER_EXPIRED", "Đơn hàng đã hết thời gian thanh toán");
      }

      // Update payment method to VNPAY inside order-service
      // (Standard payment flow updates payment method locally or during checkout)

      String vnp_TxnRef = orderId + "_" + getRandomNumber(8);
      String vnp_OrderInfo = "Thanh toan don hang #" + orderId;
      String vnp_OrderType = "other";
      String vnp_IpAddr = getIpAddress();
      long totalAmount = order.totalDiscountedPrice() != null ? order.totalDiscountedPrice() : 0;
      long amount = Math.multiplyExact(totalAmount, 100L);

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

      String vnp_CreateDate = formatVnpayDate(now);
      String vnp_ExpireDate = formatVnpayDate(order.expiresAt());

      vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
      vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

      // Find existing PaymentDetail or create a new record to avoid unique order_id constraint
      // violation
      Optional<PaymentDetail> existingPayment = paymentRepository.findByOrderId(orderId);
      PaymentDetail paymentDetail = reusablePayment(existingPayment);

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
      saveAndPublishStatus(paymentDetail);

      return vnp_PayUrl + "?" + queryUrl;
    } catch (RuntimeException e) {
      log.error("Failed to initiate VNPay payment request: {}", e.getMessage(), e);
      throw e;
    } catch (Exception e) {
      log.error("Failed to initiate VNPay payment request: {}", e.getMessage(), e);
      throw new DomainException(
          HttpStatus.BAD_GATEWAY, "DEPENDENCY_ERROR", "Không thể tạo yêu cầu thanh toán lúc này");
    }
  }

  public PaymentDetail getPaymentById(Long paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(
            () ->
                new DomainException(
                    HttpStatus.NOT_FOUND,
                    "PAYMENT_NOT_FOUND",
                    "Không tìm thấy thông tin thanh toán: " + paymentId));
  }

  @Transactional
  public PaymentDetail processPaymentCallback(Map<String, String> vnpParams) {
    String transactionRef = required(vnpParams, "vnp_TxnRef");
    validateSignature(vnpParams);

    PaymentDetail payment =
        paymentRepository
            .findByTransactionId(transactionRef)
            .orElseThrow(
                () ->
                    new DomainException(
                        HttpStatus.NOT_FOUND,
                        "TRANSACTION_NOT_FOUND",
                        "Không tìm thấy giao dịch: " + transactionRef));
    validateCallback(vnpParams, payment);

    String responseCode = vnpParams.get("vnp_ResponseCode");
    boolean completed =
        "00".equals(responseCode) && "00".equals(vnpParams.get("vnp_TransactionStatus"));
    PaymentStatus nextStatus = resolvedStatus(payment.getPaymentStatus(), completed);

    // A confirmed charge is monotonic; duplicate or late failure callbacks must not reverse it.
    if (payment.getPaymentStatus() == nextStatus) {
      return payment;
    }

    payment.setPaymentStatus(nextStatus);
    payment.setPaymentLog(new Gson().toJson(vnpParams));
    payment.setVnp_ResponseCode(responseCode);
    if (completed) payment.setPaymentDate(LocalDateTime.now());
    return saveAndPublishStatus(payment);
  }

  void validateCallback(Map<String, String> params, PaymentDetail payment) {
    if (!vnp_TmnCode.equals(required(params, "vnp_TmnCode"))) {
      throw new IllegalArgumentException("Mã website VNPay không hợp lệ");
    }
    required(params, "vnp_ResponseCode");
    required(params, "vnp_TransactionStatus");
    long amount;
    try {
      amount = Long.parseLong(required(params, "vnp_Amount"));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Số tiền VNPay không hợp lệ");
    }
    if (amount != (long) payment.getTotalAmount() * 100L) {
      throw new IllegalArgumentException("Số tiền VNPay không khớp đơn hàng");
    }
  }

  void validateSignature(Map<String, String> params) {
    String provided = required(params, "vnp_SecureHash").toLowerCase(Locale.ROOT);
    String expected = hmacSHA512(vnp_HashSecret, callbackHashData(params));
    if (!MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.US_ASCII),
        provided.getBytes(StandardCharsets.US_ASCII))) {
      throw new IllegalArgumentException("Chữ ký VNPay không hợp lệ");
    }
  }

  private String callbackHashData(Map<String, String> params) {
    return params.entrySet().stream()
        .filter(e -> e.getKey().startsWith("vnp_"))
        .filter(e -> !"vnp_SecureHash".equals(e.getKey()))
        .filter(e -> !"vnp_SecureHashType".equals(e.getKey()))
        .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
        .sorted(Map.Entry.comparingByKey())
        .map(
            e ->
                URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .reduce((left, right) -> left + "&" + right)
        .orElse("");
  }

  private static String required(Map<String, String> params, String name) {
    String value = params.get(name);
    if (value == null || value.isBlank()) throw new IllegalArgumentException("Thiếu " + name);
    return value;
  }

  static PaymentStatus resolvedStatus(PaymentStatus current, boolean completed) {
    if (current == PaymentStatus.COMPLETED) return current;
    return completed ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;
  }

  static boolean isExpired(Instant expiresAt, Instant now) {
    return expiresAt == null || !now.isBefore(expiresAt);
  }

  static String formatVnpayDate(Instant instant) {
    return VNPAY_DATE_FORMAT.format(instant);
  }

  static PaymentDetail reusablePayment(Optional<PaymentDetail> existingPayment) {
    if (existingPayment.isPresent()
        && existingPayment.get().getPaymentStatus() == PaymentStatus.COMPLETED) {
      throw new DomainException(
          HttpStatus.CONFLICT, "PAYMENT_ALREADY_COMPLETED", "Đơn hàng này đã được thanh toán.");
    }
    return existingPayment.orElseGet(PaymentDetail::new);
  }

  private PaymentDetail saveAndPublishStatus(PaymentDetail payment) {
    PaymentDetail savedPayment = paymentRepository.save(payment);
    eventPublisher.publishEvent(
        new PaymentStatusChangedEvent(
            savedPayment.getOrderId(), savedPayment.getPaymentStatus().name()));
    return savedPayment;
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
      if (ipAddress == null
          || ipAddress.isEmpty()
          || "0:0:0:0:0:0:0:1".equals(ipAddress)
          || "::1".equals(ipAddress)) {
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
      SecretKeySpec secret_key =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
      sha512_HMAC.init(secret_key);
      byte[] hash = sha512_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception ex) {
      throw new IllegalStateException("Không thể tạo chữ ký VNPay", ex);
    }
  }
}
