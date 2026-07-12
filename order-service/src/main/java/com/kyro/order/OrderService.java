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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final CatalogClient catalogClient;
  private final CartClient cartClient;
  private final UserClient userClient;
  private final RabbitTemplate rabbitTemplate;

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
    if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
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
    shippingAddress.setFullName(addrResp.getFullName());
    shippingAddress.setProvince(addrResp.getProvince());
    shippingAddress.setDistrict(addrResp.getDistrict());
    shippingAddress.setWard(addrResp.getWard());
    shippingAddress.setStreet(addrResp.getStreet());
    shippingAddress.setNote(addrResp.getNote());
    shippingAddress.setPhoneNumber(addrResp.getPhoneNumber());

    List<Order> createdOrders = new ArrayList<>();

    // Calculate pricing
    int totalOriginalPrice = 0;
    int totalDiscountedPrice = 0;
    int totalItemsCount = 0;

    for (CartClient.CartItemResponse item : cart.getItems()) {
      totalOriginalPrice += item.getPrice() * item.getQuantity();
      totalDiscountedPrice += item.getDiscountedPrice() * item.getQuantity();
      totalItemsCount += item.getQuantity();
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
    order.setPaymentMethod(PaymentMethod.COD);

    order.setOriginalPrice(totalOriginalPrice);
    order.setTotalItems(totalItemsCount);
    order.setDiscount(totalDiscount);
    order.setTotalDiscountedPrice(totalDiscountedPrice);

    Order savedOrderIntermediate = orderRepository.save(order);
    log.info("Saved intermediate order ID: {}", savedOrderIntermediate.getId());

    List<OrderItem> orderItems = new ArrayList<>();
    for (CartClient.CartItemResponse cartItem : cart.getItems()) {
      // Verify stock by calling catalog-service via FeignClient
      CatalogClient.ProductResponse product = catalogClient.getProductById(cartItem.getProductId());
      if (product == null) {
        throw new RuntimeException("Sản phẩm ID " + cartItem.getProductId() + " không tồn tại.");
      }

      String sizeName = cartItem.getSize();
      int orderedQuantity = cartItem.getQuantity();

      OrderItem orderItem = new OrderItem();
      orderItem.setOrder(savedOrderIntermediate);
      orderItem.setProductId(cartItem.getProductId());
      orderItem.setProductName(product.getTitle());
      if (product.getImages() != null && !product.getImages().isEmpty()) {
        orderItem.setProductImageUrl(product.getImages().get(0).getDownloadUrl());
      }
      orderItem.setQuantity(orderedQuantity);
      orderItem.setPrice(cartItem.getPrice());
      orderItem.setSize(sizeName);
      orderItem.setDiscountPercent(cartItem.getDiscountPercent());
      orderItem.setDiscountedPrice(cartItem.getDiscountedPrice());
      orderItem.setDeliveryDate(LocalDateTime.now().plusDays(7));
      orderItems.add(orderItem);

      // Deduct stock via FeignClient
      catalogClient.decreaseStock(cartItem.getProductId(), sizeName, orderedQuantity);
    }

    savedOrderIntermediate.setOrderItems(orderItems);
    Order finalSavedOrder = orderRepository.save(savedOrderIntermediate);
    createdOrders.add(finalSavedOrder);
    log.info("Successfully created and saved final order ID: {}", finalSavedOrder.getId());

    // Clear Cart in cart-service
    cartClient.clearCart(userId);
    log.info("Cart cleared for user ID: {} as order was created.", userId);

    // Publish Order Email Notification request to RabbitMQ
    sendOrderConfirmationEmail(userEmail, finalSavedOrder);

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

  @Transactional(readOnly = true)
  public List<Order> getAllOrders() {
    return orderRepository.findAll();
  }

  @Transactional
  public void deleteOrder(Long orderId) {
    Order order = findOrderById(orderId);
    orderRepository.delete(order);
    log.info("Order ID {} deleted.", orderId);
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
  public List<OrderDetailDTO> getAllOrdersByJF() {
    // Find all orders sorting by date descending
    List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
    return orders.stream().map(OrderDetailDTO::new).collect(Collectors.toList());
  }

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
