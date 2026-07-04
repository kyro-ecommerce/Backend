package com.kyro.order;

import com.kyro.auth.User;
import com.kyro.auth.UserService;
import com.kyro.auth.security.otp.OtpService;
import com.kyro.enums.OrderStatus;
import com.kyro.exceptions.DomainException;
import com.kyro.order.dto.OrderDTO;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final OtpService otpService;
    private final CartRepository cartRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }

        List<Order> orders = orderService.userOrderHistory(user.getId(), null);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @PostMapping("/create/{addressId}")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestHeader("Authorization") String jwt,
                                         @PathVariable("addressId") Long addressId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.warn("Unauthorized attempt to create order: No authentication found.");
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để đặt hàng.");
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            log.warn("Unauthorized attempt to create order: User not found for JWT.");
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.");
        }

        boolean addressExists = user.getAddress().stream()
                .anyMatch(address -> address.getId().equals(addressId));

        if (!addressExists) {
            log.warn("Address ID {} not found for user {}", addressId, user.getEmail());
            throw new DomainException(HttpStatus.NOT_FOUND, "Địa chỉ giao hàng không tìm thấy hoặc không thuộc về bạn.");
        }

        List<Order> orders = orderService.placeOrder(addressId, user);

        if (orders == null || orders.isEmpty()) {
            log.warn("Order creation resulted in no orders for user {}, addressId {}.", user.getEmail(), addressId);
            Cart cart = cartRepository.findByUserId(user.getId());
            if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                throw new DomainException(HttpStatus.BAD_REQUEST, "Giỏ hàng của bạn đang trống. Không thể tạo đơn hàng.");
            }
            throw new DomainException(HttpStatus.BAD_REQUEST, "Không có sản phẩm hợp lệ nào trong giỏ hàng của bạn để đặt hàng. Vui lòng kiểm tra lại.");
        }

        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("orders", orderDTOs);
        response.put("totalOrdersCreated", orders.size());
        response.put("totalAmountForAllOrders", orders.stream()
                .mapToInt(order -> order.getTotalDiscountedPrice() != null ? order.getTotalDiscountedPrice() : 0)
                .sum());
        response.put("message", "Đã tạo thành công " + orders.size() + " đơn hàng.");

        log.info("Successfully created {} order(s) for user {}", orders.size(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> findOrderById(@PathVariable("id") Long orderId) {
        Order order = orderService.findOrderById(orderId);
        OrderDTO orderDTO = new OrderDTO(order);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OrderDTO>> getPendingOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.PENDING);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/confirmed")
    public ResponseEntity<List<OrderDTO>> getConfirmedOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.CONFIRMED);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/shipped")
    public ResponseEntity<List<OrderDTO>> getShippedOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.SHIPPED);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/delivered")
    public ResponseEntity<List<OrderDTO>> getDeliveredOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.DELIVERED);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/cancelled")
    public ResponseEntity<List<OrderDTO>> getCancelledOrders(@RequestHeader("Authorization") String jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }
        List<Order> orders = orderService.userOrderHistory(user.getId(), OrderStatus.CANCELLED);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable("id") Long orderId, @RequestHeader("Authorization") String jwt) {
        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ.");
        }
        Order order = orderService.findOrderById(orderId);
        if (order == null) {
            throw new DomainException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng.");
        }
        if (!order.getUser().getId().equals(user.getId())) {
            throw new DomainException(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy đơn hàng này.");
        }

        Order cancelledOrder = orderService.cancelOrder(orderId);
        OrderDTO orderDTO = new OrderDTO(cancelledOrder);
        return ResponseEntity.ok(orderDTO);
    }

    @PostMapping("/send-mail/{orderId}")
    public ResponseEntity<Map<String, String>> sendMail(@RequestHeader("Authorization") String jwt,
                                                @PathVariable("orderId") Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User user = userService.findUserByJwt(jwt);
        if (user == null) {
            throw new DomainException(HttpStatus.NOT_FOUND, "User not found");
        }

        Order order = orderService.findOrderById(orderId);
        if (order == null) {
            throw new DomainException(HttpStatus.NOT_FOUND, "Order not found");
        }

        if (!order.getUser().getId().equals(user.getId())) {
            throw new DomainException(HttpStatus.FORBIDDEN, "You don't have permission to access this order");
        }

        otpService.sendOrderMail(user.getEmail(), order);
        return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
    }
}