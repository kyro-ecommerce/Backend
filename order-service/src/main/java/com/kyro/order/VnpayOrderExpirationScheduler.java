package com.kyro.order;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VnpayOrderExpirationScheduler {

  private static final Logger log =
      LoggerFactory.getLogger(VnpayOrderExpirationScheduler.class);
  static final Duration CALLBACK_GRACE_PERIOD = Duration.ofMinutes(5);
  private static final int BATCH_SIZE = 100;

  private final OrderRepository orderRepository;
  private final OrderService orderService;

  public VnpayOrderExpirationScheduler(
      OrderRepository orderRepository, OrderService orderService) {
    this.orderRepository = orderRepository;
    this.orderService = orderService;
  }

  @Scheduled(fixedDelay = 30_000)
  public void expireOrders() {
    Instant cutoff = expirationCutoff(Instant.now());
    List<Long> orderIds =
        orderRepository.findExpiredVnpayOrderIds(cutoff, PageRequest.of(0, BATCH_SIZE));
    for (Long orderId : orderIds) {
      try {
        orderService.expireVnpayOrder(orderId, cutoff);
      } catch (RuntimeException exception) {
        log.error("Failed to expire VNPAY order {}; it will be retried.", orderId, exception);
      }
    }
  }

  static Instant expirationCutoff(Instant now) {
    return now.minus(CALLBACK_GRACE_PERIOD);
  }
}
