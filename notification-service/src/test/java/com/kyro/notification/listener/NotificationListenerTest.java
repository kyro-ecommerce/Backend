package com.kyro.notification.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kyro.notification.service.EmailService;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NotificationListenerTest {

  @Test
  void sendsConfirmedOrderOnly() {
    RecordingEmailService emailService = new RecordingEmailService();
    NotificationListener listener = new NotificationListener(emailService);
    Map<String, Object> confirmed =
        Map.of("id", 1L, "orderCode", "KYR-A1B2C3D4E5F6", "status", "CONFIRMED");
    Map<String, Object> pending =
        Map.of("id", 2L, "orderCode", "KYR-ABCDEF123456", "status", "PENDING");

    listener.receiveOrderNotification(Map.of("email", "customer@example.com", "order", confirmed));
    listener.receiveOrderNotification(Map.of("email", "customer@example.com", "order", pending));

    assertEquals(1, emailService.sent);
    assertEquals(confirmed, emailService.order);
  }

  private static final class RecordingEmailService extends EmailService {
    private int sent;
    private Map<String, Object> order;

    private RecordingEmailService() {
      super(null, null);
    }

    @Override
    public void sendOrderMail(String email, Map<String, Object> orderData) {
      sent++;
      order = orderData;
    }
  }
}
