package com.kyro.order;

import com.kyro.enums.OrderStatus;
import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.client.CartClient;
import com.kyro.order.client.CatalogClient;
import com.kyro.order.client.UserClient;
import com.kyro.order.dto.OrderDTO;
import com.kyro.order.dto.OrderDetailDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class managing ordering workflow, integrated with external catalog, cart, auth
 * microservices and RabbitMQ.
 */
@Service
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  private final OrderRepository orderRepository;
  private final CatalogClient catalogClient;
  private final CartClient cartClient;
  private final UserClient userClient;
  private final RabbitTemplate rabbitTemplate;

  public OrderService(
      OrderRepository orderRepository,
      CatalogClient catalogClient,
      CartClient cartClient,
      UserClient userClient,
      RabbitTemplate rabbitTemplate) {
    this.orderRepository = orderRepository;
    this.catalogClient = catalogClient;
    this.cartClient = cartClient;
    this.userClient = userClient;
    this.rabbitTemplate = rabbitTemplate;
  }

  public OrderDTO convertToDto(Order order) {
    return new OrderDTO(order);
  }

  public Order findOrderById(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(
            () -> {
              log.warn("Order not found with ID: {}", orderId);
              return new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId);
            });
  }

  public List<Order> userOrderHistory(Long userId, OrderStatus status) {
    if (status != null) {
      return orderRepository.findByUserIdAndOrderStatus(userId, status);
    } else {
      return orderRepository.findByUserId(userId);
    }
  }

  @Transactional
  public List<Order> placeOrder(Long addressId, Long userId, String userEmail) {
    return placeOrder(addressId, userId, userEmail, PaymentMethod.COD);
  }

  @Transactional
  public List<Order> placeOrder(
      Long addressId, Long userId, String userEmail, PaymentMethod paymentMethod) {
    if (userId == null) {
      log.error("User ID is null when placing order.");
      throw new IllegalArgumentException("Thông tin người dùng không hợp lệ.");
    }
    if (addressId == null) {
      log.error("Address ID is null when placing order for user: {}", userId);
      throw new IllegalArgumentException("Địa chỉ giao hàng không được để trống.");
    }

    // Fetch Cart from cart-service via FeignClient
    CartClient.CartResponse cart = cartClient.getCart(userId);
    if (cart == null || cart.items() == null || cart.items().isEmpty()) {
      log.warn("Attempted to place order with an empty cart for user ID: {}", userId);
      throw new RuntimeException(
          "Giỏ hàng của bạn đang trống. Vui lòng thêm sản phẩm vào giỏ hàng trước khi đặt hàng.");
    }

    // Fetch Shipping Address from auth-service via FeignClient
    UserClient.AddressResponse addrResp = userClient.getAddressById(addressId, userId);
    if (addrResp == null) {
      log.warn("Address not found with ID: {} for user ID: {}", addressId, userId);
      throw new RuntimeException("Địa chỉ giao hàng không hợp lệ.");
    }

    Address shippingAddress = new Address();
    shippingAddress.setFullName(addrResp.fullName());
    shippingAddress.setProvince(addrResp.province());
    shippingAddress.setDistrict(addrResp.district());
    shippingAddress.setWard(addrResp.ward());
    shippingAddress.setStreet(addrResp.street());
    shippingAddress.setNote(addrResp.note());
    shippingAddress.setPhoneNumber(addrResp.phoneNumber());

    List<Order> createdOrders = new ArrayList<>();

    // Calculate pricing
    int totalOriginalPrice = 0;
    int totalDiscountedPrice = 0;
    int totalItemsCount = 0;

    for (CartClient.CartItemResponse item : cart.items()) {
      totalOriginalPrice += item.price() * item.quantity();
      totalDiscountedPrice += item.discountedPrice() * item.quantity();
      totalItemsCount += item.quantity();
    }
    int totalDiscount = totalOriginalPrice - totalDiscountedPrice;

    // Create Order Entity
    Order order = new Order();
    order.setUserId(userId);
    order.setUserEmail(userEmail);
    order.setOrderDate(LocalDateTime.now());
    order.setShippingAddress(shippingAddress);
    order.setOrderStatus(OrderStatus.PENDING);
    order.setPaymentStatus(PaymentStatus.PENDING);
    order.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.COD);

    order.setOriginalPrice(totalOriginalPrice);
    order.setTotalItems(totalItemsCount);
    order.setDiscount(totalDiscount);
    order.setTotalDiscountedPrice(totalDiscountedPrice);

    Order savedOrderIntermediate = orderRepository.save(order);
    log.info("Saved intermediate order ID: {}", savedOrderIntermediate.getId());

    List<OrderItem> orderItems = new ArrayList<>();
    List<com.kyro.order.event.OrderCreatedEvent.OrderItemEvent> eventItems = new ArrayList<>();

    for (CartClient.CartItemResponse cartItem : cart.items()) {
      CatalogClient.ProductResponse product = catalogClient.getProductById(cartItem.productId());
      String productTitle = product != null ? product.title() : cartItem.productName();
      String imageUrl =
          (product != null && product.images() != null && !product.images().isEmpty())
              ? product.images().get(0).downloadUrl()
              : cartItem.productImageUrl();

      OrderItem orderItem = new OrderItem();
      orderItem.setOrder(savedOrderIntermediate);
      orderItem.setProductId(cartItem.productId());
      orderItem.setProductName(productTitle);
      orderItem.setProductImageUrl(imageUrl);
      orderItem.setQuantity(cartItem.quantity());
      orderItem.setPrice(cartItem.price());
      orderItem.setSize(cartItem.size());
      orderItem.setDiscountPercent(cartItem.discountPercent());
      orderItem.setDiscountedPrice(cartItem.discountedPrice());
      orderItem.setDeliveryDate(LocalDateTime.now().plusDays(7));
      orderItems.add(orderItem);

      eventItems.add(
          new com.kyro.order.event.OrderCreatedEvent.OrderItemEvent(
              cartItem.productId(), cartItem.size(), cartItem.quantity(), cartItem.price()));
    }

    savedOrderIntermediate.setOrderItems(orderItems);
    Order finalSavedOrder = orderRepository.save(savedOrderIntermediate);
    createdOrders.add(finalSavedOrder);
    log.info("Successfully created order ID: {} with status PENDING", finalSavedOrder.getId());

    // Publish OrderCreatedEvent to RabbitMQ for Async Stock Deduct & Cart Clear (Saga Pattern)
    try {
      com.kyro.order.event.OrderCreatedEvent orderCreatedEvent =
          new com.kyro.order.event.OrderCreatedEvent(
              finalSavedOrder.getId(), userId, userEmail, eventItems);

      rabbitTemplate.convertAndSend(
          com.kyro.order.config.RabbitMQConfig.ORDER_EXCHANGE,
          com.kyro.order.config.RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
          orderCreatedEvent);

      log.info("Published OrderCreatedEvent for Order ID #{} to RabbitMQ", finalSavedOrder.getId());
    } catch (Exception e) {
      log.error(
          "Failed to publish OrderCreatedEvent for Order ID #{}: {}",
          finalSavedOrder.getId(),
          e.getMessage(),
          e);
    }

    return createdOrders;
  }

  @Transactional
  public Order confirmedOrder(Long orderId) {
    Order order = findOrderById(orderId);
    if (order.getOrderStatus() != OrderStatus.PENDING) {
      throw new RuntimeException(
          "Đơn hàng không thể xác nhận ở trạng thái hiện tại (" + order.getOrderStatus() + ")");
    }
    order.setOrderStatus(OrderStatus.CONFIRMED);
    log.info("Order ID {} confirmed.", orderId);
    return orderRepository.save(order);
  }

  @Transactional
  public Order shippedOrder(Long orderId) {
    Order order = findOrderById(orderId);
    if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
      throw new RuntimeException(
          "Đơn hàng phải được xác nhận trước khi gửi (trạng thái hiện tại: "
              + order.getOrderStatus()
              + ")");
    }
    order.setOrderStatus(OrderStatus.SHIPPED);
    log.info("Order ID {} shipped.", orderId);
    return orderRepository.save(order);
  }

  @Transactional
  public Order deliveredOrder(Long orderId) {
    Order order = findOrderById(orderId);
    if (order.getOrderStatus() != OrderStatus.SHIPPED) {
      throw new RuntimeException(
          "Đơn hàng phải được gửi trước khi giao (trạng thái hiện tại: "
              + order.getOrderStatus()
              + ")");
    }
    order.setOrderStatus(OrderStatus.DELIVERED);
    order.setPaymentStatus(PaymentStatus.COMPLETED);
    order.setDeliveryDate(LocalDateTime.now());
    log.info("Order ID {} delivered.", orderId);
    return orderRepository.save(order);
  }

  @Transactional
  public Order cancelOrder(Long orderId) {
    Order order = findOrderById(orderId);
    if (order.getOrderStatus() == OrderStatus.DELIVERED
        || order.getOrderStatus() == OrderStatus.CANCELLED) {
      throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái " + order.getOrderStatus());
    }

    if (order.getOrderStatus() == OrderStatus.PENDING
        || order.getOrderStatus() == OrderStatus.CONFIRMED) {
      for (OrderItem orderItem : order.getOrderItems()) {
        // Restore stock in catalog-service via FeignClient
        catalogClient.increaseStock(
            orderItem.getProductId(), orderItem.getSize(), orderItem.getQuantity());
        log.info(
            "Restored stock for Product ID {}: Size {} quantity increased by {}.",
            orderItem.getProductId(),
            orderItem.getSize(),
            orderItem.getQuantity());
      }
    }

    order.setOrderStatus(OrderStatus.CANCELLED);

    if (order.getPaymentMethod() == PaymentMethod.VNPAY
        && order.getPaymentStatus() == PaymentStatus.COMPLETED) {
      order.setPaymentStatus(PaymentStatus.REFUNDED);
      log.info("Order ID {} cancelled. Payment status set to REFUNDED for VNPAY.", orderId);
    } else {
      order.setPaymentStatus(PaymentStatus.CANCELLED);
      log.info("Order ID {} cancelled. Payment status set to CANCELLED.", orderId);
    }

    return orderRepository.save(order);
  }

  @Transactional
  public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order = findOrderById(orderId);
    if (order.getOrderStatus() == newStatus) {
      return order;
    }
    if (newStatus == OrderStatus.CANCELLED) {
      return cancelOrder(orderId);
    } else if (newStatus == OrderStatus.DELIVERED) {
      order.setOrderStatus(OrderStatus.DELIVERED);
      order.setPaymentStatus(PaymentStatus.COMPLETED);
      order.setDeliveryDate(LocalDateTime.now());
      log.info("Order ID {} marked as DELIVERED by admin.", orderId);
      return orderRepository.save(order);
    } else {
      order.setOrderStatus(newStatus);
      log.info("Order ID {} status changed to {} by admin.", orderId, newStatus);
      return orderRepository.save(order);
    }
  }

  @Transactional(readOnly = true)
  public List<Order> getAllOrders() {
    return orderRepository.findAll();
  }

  @Transactional
  public void deleteOrder(Long orderId) {
    Order order = findOrderById(orderId);
    orderRepository.delete(order);
    log.info("Order ID {} deleted successfully.", orderId);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getOrderStatistics(LocalDate start, LocalDate end) {
    LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
    LocalDateTime endDateTime = end != null ? end.atTime(23, 59, 59) : null;

    long totalOrders =
        orderRepository.countOrdersByStatusAndDateRange(null, startDateTime, endDateTime);
    long pendingOrders =
        orderRepository.countOrdersByStatusAndDateRange(
            OrderStatus.PENDING, startDateTime, endDateTime);
    long confirmedOrders =
        orderRepository.countOrdersByStatusAndDateRange(
            OrderStatus.CONFIRMED, startDateTime, endDateTime);
    long deliveredOrders =
        orderRepository.countOrdersByStatusAndDateRange(
            OrderStatus.DELIVERED, startDateTime, endDateTime);
    long cancelledOrders =
        orderRepository.countOrdersByStatusAndDateRange(
            OrderStatus.CANCELLED, startDateTime, endDateTime);

    Double totalRevenue = orderRepository.sumRevenueByDateRange(startDateTime, endDateTime);
    totalRevenue = totalRevenue != null ? totalRevenue : 0.0;

    double averageOrderValue = deliveredOrders > 0 ? totalRevenue / deliveredOrders : 0;

    Map<String, Object> result = new HashMap<>();
    result.put("totalOrders", totalOrders);
    result.put("pendingOrders", pendingOrders);
    result.put("confirmedOrders", confirmedOrders);
    result.put("completedOrders", deliveredOrders);
    result.put("cancelledOrders", cancelledOrders);
    result.put("totalRevenue", totalRevenue);
    result.put("averageOrderValue", averageOrderValue);

    return result;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> getDailyRevenue(LocalDate start, LocalDate end) {
    LocalDateTime startDateTime = start != null ? start.atStartOfDay() : null;
    LocalDateTime endDateTime = end != null ? end.atTime(23, 59, 59) : null;

    List<Object[]> rows = orderRepository.findDailyRevenueByDateRange(startDateTime, endDateTime);
    List<Map<String, Object>> result = new ArrayList<>();
    for (Object[] row : rows) {
      String dateStr = row[0] != null ? row[0].toString() : "";
      Number revenueNum = (Number) row[1];
      result.add(
          Map.of("name", dateStr, "revenue", revenueNum != null ? revenueNum.doubleValue() : 0.0));
    }
    return result;
  }

  @Transactional(readOnly = true)
  public List<OrderDetailDTO> getAllOrdersByJF() {
    // Find all orders sorting by date descending
    List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
    return orders.stream().map(OrderDetailDTO::new).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Page<OrderDetailDTO> getAllOrdersWithFilters(
      String search,
      OrderStatus status,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable) {
    LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
    LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

    Page<Order> orders =
        orderRepository.findAdminOrdersWithFilters(
            search, status, startDateTime, endDateTime, pageable);

    return orders.map(OrderDetailDTO::new);
  }

  private void sendOrderConfirmationEmail(String email, Order order) {
    try {
      // Build simple order detail map representation for email template context
      Map<String, Object> orderMap = new HashMap<>();
      orderMap.put("id", order.getId());
      orderMap.put("totalDiscountedPrice", order.getTotalDiscountedPrice());
      orderMap.put("paymentMethod", order.getPaymentMethod().name());
      orderMap.put("orderDate", order.getOrderDate().toString());

      Map<String, Object> addressMap = new HashMap<>();
      addressMap.put("fullName", order.getShippingAddress().getFullName());
      addressMap.put("phoneNumber", order.getShippingAddress().getPhoneNumber());
      addressMap.put("street", order.getShippingAddress().getStreet());
      addressMap.put("ward", order.getShippingAddress().getWard());
      addressMap.put("district", order.getShippingAddress().getDistrict());
      addressMap.put("province", order.getShippingAddress().getProvince());
      orderMap.put("shippingAddress", addressMap);

      List<Map<String, Object>> itemsList =
          order.getOrderItems().stream()
              .map(
                  item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productName", item.getProductName());
                    itemMap.put("size", item.getSize());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice());
                    itemMap.put("discountedPrice", item.getDiscountedPrice());
                    itemMap.put("productImageUrl", item.getProductImageUrl());
                    return itemMap;
                  })
              .collect(Collectors.toList());
      orderMap.put("orderItems", itemsList);

      Map<String, Object> payload =
          Map.of(
              "email", email,
              "order", orderMap);

      rabbitTemplate.convertAndSend("notification-exchange", "notification.order", payload);
      log.info(
          "Published order confirmation notification event to RabbitMQ for order #{}",
          order.getId());
    } catch (Exception e) {
      log.error("Failed to publish order confirmation notification event: {}", e.getMessage(), e);
    }
  }

  @Transactional
  public void updatePaymentStatus(Long orderId, PaymentStatus status) {
    Order order = findOrderById(orderId);
    order.setPaymentStatus(status);
    if (status == PaymentStatus.COMPLETED) {
      order.setOrderStatus(OrderStatus.CONFIRMED);
    }
    orderRepository.save(order);
    log.info("Successfully updated payment status for order ID {} to {}", orderId, status);
  }
}
