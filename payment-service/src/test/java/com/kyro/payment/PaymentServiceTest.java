package com.kyro.payment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kyro.enums.PaymentStatus;
import com.kyro.exceptions.DomainException;
import com.kyro.payment.client.OrderClient;
import com.kyro.payment.event.PaymentStatusChangedEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PaymentServiceTest {

  private static final String SECRET = "test-secret";
  private PaymentService service;

  @BeforeEach
  void setUp() {
    service = new PaymentService(null, null, null);
    ReflectionTestUtils.setField(service, "vnp_TmnCode", "TESTCODE");
    ReflectionTestUtils.setField(service, "vnp_HashSecret", SECRET);
  }

  @Test
  void validatesSignedCallbackAndAmount() {
    PaymentDetail payment = payment();
    Map<String, String> params = signedParams();

    assertDoesNotThrow(() -> service.validateSignature(params));
    assertDoesNotThrow(() -> service.validateCallback(params, payment));

    params.put("vnp_Amount", "9999");
    params.put("vnp_SecureHash", sign(params));
    assertThrows(IllegalArgumentException.class, () -> service.validateCallback(params, payment));
  }

  @Test
  void rejectsInvalidSignatureAndNeverRegressesCompletedPayment() {
    Map<String, String> params = signedParams();
    params.put("vnp_SecureHash", "bad-signature");

    assertThrows(IllegalArgumentException.class, () -> service.validateSignature(params));
    assertEquals(
        PaymentStatus.COMPLETED, PaymentService.resolvedStatus(PaymentStatus.FAILED, true));
    assertEquals(
        PaymentStatus.COMPLETED, PaymentService.resolvedStatus(PaymentStatus.COMPLETED, false));
  }

  @Test
  void reusesRetryablePaymentButRejectsCompletedPayment() {
    PaymentDetail pending = new PaymentDetail();
    pending.setPaymentStatus(PaymentStatus.PENDING);
    assertSame(pending, PaymentService.reusablePayment(Optional.of(pending)));

    pending.setPaymentStatus(PaymentStatus.COMPLETED);
    DomainException exception =
        assertThrows(
            DomainException.class, () -> PaymentService.reusablePayment(Optional.of(pending)));
    assertEquals(409, exception.getStatus().value());
    assertEquals("PAYMENT_ALREADY_COMPLETED", exception.getErrorCode());
  }

  @Test
  void formatsOrderExpirationInVietnamTimezone() {
    Instant expiration = Instant.parse("2026-08-16T08:15:30Z");

    assertEquals("20260816151530", PaymentService.formatVnpayDate(expiration));
    assertEquals(false, PaymentService.isExpired(expiration, expiration.minusSeconds(1)));
    assertEquals(true, PaymentService.isExpired(expiration, expiration));
  }

  @Test
  void retryResetsPaymentToPendingAndPublishesProjectionUpdate() {
    PaymentDetail failedPayment = new PaymentDetail();
    failedPayment.setOrderId(42L);
    failedPayment.setPaymentStatus(PaymentStatus.FAILED);
    failedPayment.setTransactionId("42_old");
    Instant expiresAt = Instant.now().plusSeconds(15 * 60);
    OrderClient orderClient =
        ignored ->
            new OrderClient.OrderResponse(42L, 7L, 100L, "FAILED", "VNPAY", "PENDING", expiresAt);
    PaymentRepository repository = repositoryFor(failedPayment);
    List<Object> events = new ArrayList<>();
    PaymentService retryService = new PaymentService(orderClient, repository, events::add);
    ReflectionTestUtils.setField(retryService, "vnp_TmnCode", "TESTCODE");
    ReflectionTestUtils.setField(retryService, "vnp_HashSecret", SECRET);
    ReflectionTestUtils.setField(retryService, "vnp_PayUrl", "https://sandbox.vnpayment.vn/pay");
    ReflectionTestUtils.setField(retryService, "vnp_Returnurl", "https://kyro.test/callback");

    String paymentUrl = retryService.createPayment(42L);

    assertTrue(paymentUrl.startsWith("https://sandbox.vnpayment.vn/pay?"));
    assertEquals(PaymentStatus.PENDING, failedPayment.getPaymentStatus());
    assertNotEquals("42_old", failedPayment.getTransactionId());
    PaymentStatusChangedEvent event = (PaymentStatusChangedEvent) events.getFirst();
    assertEquals(42L, event.orderId());
    assertEquals("PENDING", event.status());
  }

  private static PaymentDetail payment() {
    PaymentDetail payment = new PaymentDetail();
    payment.setTotalAmount(100);
    return payment;
  }

  private static Map<String, String> signedParams() {
    Map<String, String> params = new HashMap<>();
    params.put("vnp_TmnCode", "TESTCODE");
    params.put("vnp_TxnRef", "42_ref");
    params.put("vnp_Amount", "10000");
    params.put("vnp_ResponseCode", "00");
    params.put("vnp_TransactionStatus", "00");
    params.put("vnp_SecureHash", sign(params));
    return params;
  }

  private static PaymentRepository repositoryFor(PaymentDetail payment) {
    return (PaymentRepository)
        java.lang.reflect.Proxy.newProxyInstance(
            PaymentRepository.class.getClassLoader(),
            new Class<?>[] {PaymentRepository.class},
            (proxy, method, args) ->
                switch (method.getName()) {
                  case "findByOrderId" -> Optional.of(payment);
                  case "save" -> args[0];
                  default -> throw new UnsupportedOperationException(method.getName());
                });
  }

  private static String sign(Map<String, String> params) {
    try {
      StringBuilder data = new StringBuilder();
      for (Map.Entry<String, String> entry : new TreeMap<>(params).entrySet()) {
        if ("vnp_SecureHash".equals(entry.getKey())) continue;
        if (data.length() > 0) data.append('&');
        data.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
        data.append('=').append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
      }
      Mac mac = Mac.getInstance("HmacSHA512");
      mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
      return java.util.HexFormat.of()
          .formatHex(mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
