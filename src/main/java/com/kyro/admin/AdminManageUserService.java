package com.kyro.admin;

import com.kyro.auth.AddressRepository;
import com.kyro.auth.Role;
import com.kyro.auth.RoleRepository;
import com.kyro.auth.User;
import com.kyro.auth.UserRepository;
import com.kyro.auth.dto.UpdateUserInfoRequest;
import com.kyro.auth.dto.UserDTO;
import com.kyro.catalog.ReviewRepository;
import com.kyro.enums.OrderStatus;
import com.kyro.enums.UserRole;
import com.kyro.order.CartItemRepository;
import com.kyro.order.CartRepository;
import com.kyro.order.Order;
import com.kyro.order.OrderItemRepository;
import com.kyro.order.OrderRepository;
import com.kyro.payment.PaymentDetailRepository;



import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminManageUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentDetailRepository paymentDetailRepository;

        public Page<UserDTO> getAllUsers(int page, int size, String search, String role) {
        Pageable pageable = PageRequest.of(page, size);
        // Filter logic
        List<User> users;

        // remove admin account
        if (StringUtils.hasText(search) && StringUtils.hasText(role)) {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            // Search by name/email and filter by role
            users = userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleName(
                    search, search, search, userRole, pageable);
        } else if (StringUtils.hasText(search)) {
            // Only search
            users = userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContainingAndRoleNameNot(
                    search, search, search, UserRole.ADMIN, pageable);
        } else if (StringUtils.hasText(role)) {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            // Only filter by role
            users = userRepository.findByRoleName(userRole, pageable);
        } else {
            // No filters - nhưng vẫn loại trừ ADMIN
            users = userRepository.findByRoleNameNot(UserRole.ADMIN, pageable);
        }

        List<UserDTO> userDTOS = users.stream()
                .map(this::convertToDto)
                .toList();

        return new PageImpl<>(userDTOS, pageable, userRepository.count());
    }

        public UserDTO getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserDTO dto = convertToDto(user);
        long orderCount = orderRepository.findByUserId(userId).size();
        BigDecimal totalSpent = calculateCustomerSpending(userId);
        dto.setOrderCount(orderCount);
        dto.setTotalSpent(totalSpent);
        return dto;
    }

        @Transactional
    public UserDTO updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getMobile() != null) {
            user.setPhone(request.getMobile());
        }

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

        @Transactional
    public UserDTO changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserRole userRole;
        try {
            userRole = UserRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleName);
        }

        Role role = roleRepository.findByName(userRole)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        user.setRole(role);
        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

        @Transactional
    public UserDTO updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setActive(active);
        // if blocked -> banned
        if (!active) {
            user.setBanned(true);
        }

        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

        @Transactional
    public UserDTO banUser(Long userId, boolean banned) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setBanned(banned);
        if (!banned) {
            user.setActive(true);
        }
        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

        @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Xóa cart items trước khi xóa cart
        if (user.getCart() != null) {
            user.getCart().getCartItems().clear(); // Xóa CartItem khỏi Cart
            cartItemRepository.deleteByCartId(user.getCart().getId()); // Xóa CartItem trong DB
            cartRepository.delete(user.getCart());
        }

        // Lấy danh sách các Order ID của user
        List<Long> orderIds = orderRepository.findByUserId(userId).stream()
                .map(Order::getId)
                .collect(Collectors.toList());

        if (!orderIds.isEmpty()) {
            // Xóa PaymentDetails liên quan đến các Order đó
            for (Long orderId : orderIds) {
                paymentDetailRepository.findByOrderId(orderId).ifPresent(paymentDetailRepository::delete);
            }
            // Xóa OrderItems liên quan đến các Order đó
            orderItemRepository.deleteByOrderUserId(userId); // Query này có thể đã xử lý nếu OrderItem có cascade từ Order
            // Xóa Orders
            orderRepository.deleteByUserId(userId);
        }

        addressRepository.deleteByUserId(userId);
        reviewRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

        public Map<String, Object> getCustomerStatistics() {
        Map<String, Object> res = new HashMap<>();

        long totalCustomers = userRepository.countByRoleName(UserRole.CUSTOMER);

        res.put("totalCustomers", totalCustomers);

        return res;
    }

    private BigDecimal calculateCustomerSpending(Long customerId) {
        List<Order> customerOrders = orderRepository.findByUserId(customerId);

        return customerOrders.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED)
                .map(order -> BigDecimal.valueOf(order.getTotalDiscountedPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private UserDTO convertToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMobile(user.getPhone());
        dto.setActive(user.isActive());
        dto.setBanned(user.isBanned());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getRole() != null) {
            dto.setRole(user.getRole().getName().toString());
        }

        return dto;
    }

}
