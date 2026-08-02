package com.kyro.cart.messaging;

import com.kyro.cart.config.RabbitMQConfig;
import com.kyro.cart.service.CartService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

  private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

  private final CartService cartService;

  public OrderEventListener(CartService cartService) {
    this.cartService = cartService;
  }

  @RabbitListener(queues = RabbitMQConfig.CART_CLEAR_QUEUE)
  public void handleStockReserved(Map<String, Object> event) {
    Long userId = event.get("userId") != null ? Long.valueOf(event.get("userId").toString()) : null;
    Long orderId = event.get("orderId") != null ? Long.valueOf(event.get("orderId").toString()) : null;

    if (userId != null) {
      cartService.clearCart(userId.toString());
      log.info("Cleared cart for User ID #{} following stock reservation for Order ID #{}", userId, orderId);
    }
  }
}
