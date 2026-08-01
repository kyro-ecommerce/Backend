package com.kyro.catalog.messaging;

import com.kyro.catalog.Product;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes product lifecycle events to RabbitMQ for consumption by the AI Service.
 *
 * <p>Exchange: {@code product.events} (topic) <br>
 * Routing keys: {@code product.created}, {@code product.updated}, {@code product.deleted}
 *
 * <p>The AI Service listens on queue {@code ai.product.events} bound to the above routing keys, and
 * uses the events to keep its vector search index in sync.
 */
@Component
public class ProductEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(ProductEventPublisher.class);

  private final RabbitTemplate rabbitTemplate;

  public ProductEventPublisher(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  public static final String PRODUCT_EXCHANGE = "product.events";
  public static final String ROUTING_KEY_CREATED = "product.created";
  public static final String ROUTING_KEY_UPDATED = "product.updated";
  public static final String ROUTING_KEY_DELETED = "product.deleted";

  /** Publishes a PRODUCT_CREATED event after a new product is persisted. */
  public void publishProductCreated(Product product) {
    Map<String, Object> payload = buildEventPayload(product, "ProductCreated");
    send(ROUTING_KEY_CREATED, payload, product.getId());
  }

  /** Publishes a PRODUCT_UPDATED event after a product is modified. */
  public void publishProductUpdated(Product product) {
    Map<String, Object> payload = buildEventPayload(product, "ProductUpdated");
    send(ROUTING_KEY_UPDATED, payload, product.getId());
  }

  /**
   * Publishes a PRODUCT_DELETED event when a product is removed. Sends a minimal payload containing
   * only the product_id so the AI Service can deactivate it.
   */
  public void publishProductDeleted(Long productId) {
    Map<String, Object> data = new HashMap<>();
    data.put("product_id", productId);
    data.put("title", "deleted");
    data.put("is_active", false);

    Map<String, Object> payload = new HashMap<>();
    payload.put("event_id", "evt-" + UUID.randomUUID());
    payload.put("event_type", "ProductDeleted");
    payload.put("occurred_at", Instant.now().toString());
    payload.put("data", data);

    send(ROUTING_KEY_DELETED, payload, productId);
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  /**
   * Builds the full ProductEvent payload that matches the AI Service's {@code ProductEvent} schema:
   *
   * <pre>
   * {
   *   event_id:    String (UUID)
   *   event_type:  String ("ProductCreated" | "ProductUpdated" | "ProductDeleted")
   *   occurred_at: ISO-8601 timestamp
   *   data: {
   *     product_id, title, category_id, category_name, brand,
   *     original_price, discounted_price, discount_percent,
   *     average_rating, num_ratings, image_url, is_active
   *   }
   * }
   * </pre>
   */
  private Map<String, Object> buildEventPayload(Product product, String eventType) {
    // --- data block ---
    Map<String, Object> data = new HashMap<>();
    data.put("product_id", product.getId()); // AI AliasChoices: "id"
    data.put("title", product.getTitle());
    data.put("brand", product.getBrand());
    data.put("original_price", product.getPrice()); // AI AliasChoices: "price"
    data.put("discounted_price", product.getDiscountedPrice());
    data.put(
        "discount_percent", product.getDiscountPersent()); // AI AliasChoices: "discount_persent"
    data.put("average_rating", product.getAverageRating());
    data.put("num_ratings", product.getNumRatings());
    data.put("is_active", true);

    // Category
    if (product.getCategory() != null) {
      data.put("category_id", product.getCategory().getId());
      data.put("category_name", product.getCategory().getName()); // AI AliasChoices: "category"
    }

    // First image URL
    if (product.getImages() != null && !product.getImages().isEmpty()) {
      String imageUrl = product.getImages().get(0).getDownloadUrl();
      data.put("image_url", imageUrl);
    }

    // --- event envelope ---
    Map<String, Object> payload = new HashMap<>();
    payload.put("event_id", "evt-" + UUID.randomUUID());
    payload.put("event_type", eventType);
    payload.put("occurred_at", Instant.now().toString());
    payload.put("data", data);

    return payload;
  }

  private void send(String routingKey, Map<String, Object> payload, Long productId) {
    try {
      rabbitTemplate.convertAndSend(PRODUCT_EXCHANGE, routingKey, payload);
      log.info(
          "Published {} event for product ID {} to exchange '{}'",
          routingKey,
          productId,
          PRODUCT_EXCHANGE);
    } catch (Exception ex) {
      // Non-blocking: log but do not fail the main transaction
      log.error(
          "Failed to publish {} event for product ID {}: {}",
          routingKey,
          productId,
          ex.getMessage(),
          ex);
    }
  }
}
