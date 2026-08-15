package com.kyro.auth;

import com.kyro.auth.dto.BasicUserDTO;
import com.kyro.auth.dto.ChangeRoleRequest;
import com.kyro.auth.dto.UpdateUserRequest;
import com.kyro.auth.dto.UpdateUserStatusRequest;
import com.kyro.enums.UserRole;
import com.kyro.exceptions.DomainException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix:/api/v1}/admin/users")
@Transactional
public class AdminUserController {

  private final UserRepository userRepository;
  private final UserService userService;

  public AdminUserController(UserRepository userRepository, UserService userService) {
    this.userRepository = userRepository;
    this.userService = userService;
  }

  @GetMapping
  @Transactional(readOnly = true)
  public ResponseEntity<Page<BasicUserDTO>> getAllUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String role,
      @RequestParam(defaultValue = "all") String status,
      @RequestParam(required = false) List<String> sort) {

    Pageable pageable = userPageable(page, size, sort);
    String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    UserRole userRoleEnum = null;
    if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("all")) {
      try {
        userRoleEnum = UserRole.valueOf(role.trim().toUpperCase());
      } catch (IllegalArgumentException ignored) {
      }
    }

    Boolean banned =
        switch (status.trim().toLowerCase()) {
          case "all", "" -> null;
          case "active" -> false;
          case "banned" -> true;
          default -> throw new IllegalArgumentException("Unsupported user status: " + status);
        };
    Page<User> usersPage =
        userRepository.findAdminUsersWithFilters(cleanSearch, userRoleEnum, banned, pageable);
    Page<BasicUserDTO> dtoList = usersPage.map(userService::convertToBasicDto);

    return ResponseEntity.ok(dtoList);
  }

  static Pageable userPageable(int page, int size, List<String> values) {
    if (page < 0 || size < 1 || size > 100)
      throw new IllegalArgumentException("Invalid page or size");
    List<String> tokens =
        values == null
            ? List.of()
            : values.stream()
                .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    if (tokens.size() % 2 != 0)
      throw new IllegalArgumentException("sort must use field,direction pairs");
    Map<String, String> fields =
        Map.of("id", "id", "email", "email", "name", "firstName", "createdAt", "createdAt");
    List<Sort.Order> orders = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i += 2) {
      String field = fields.get(tokens.get(i));
      if (field == null)
        throw new IllegalArgumentException("Unsupported user sort: " + tokens.get(i));
      Sort.Direction direction = Sort.Direction.fromString(tokens.get(i + 1));
      Sort.Order order = new Sort.Order(direction, field);
      orders.add(("email".equals(tokens.get(i)) || "name".equals(tokens.get(i))) ? order.ignoreCase() : order);
      if ("name".equals(tokens.get(i)))
        orders.add(new Sort.Order(direction, "lastName").ignoreCase());
    }
    if (orders.isEmpty()) orders.add(Sort.Order.asc("id"));
    if (orders.stream().noneMatch(order -> order.getProperty().equals("id")))
      orders.add(new Sort.Order(orders.getFirst().getDirection(), "id"));
    return PageRequest.of(page, size, Sort.by(orders));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<BasicUserDTO> getUserDetails(@PathVariable Long userId) {
    User user = userService.getUserById(userId);
    return ResponseEntity.ok(userService.convertToBasicDto(user));
  }

  @PatchMapping("/{userId}")
  public ResponseEntity<BasicUserDTO> updateUser(
      @PathVariable Long userId, @RequestBody UpdateUserRequest request) {
    userService.updateUser(request, userId);
    return ResponseEntity.ok(userService.convertToBasicDto(userService.getUserById(userId)));
  }

  @PatchMapping("/{userId}/role")
  public ResponseEntity<BasicUserDTO> changeRole(
      @PathVariable Long userId, @RequestBody ChangeRoleRequest request) {
    BasicUserDTO updated = userService.changeUserRole(userId, request.getRole());
    return ResponseEntity.ok(updated);
  }

  @PatchMapping("/{userId}/status")
  public ResponseEntity<Map<String, String>> changeStatus(
      @PathVariable Long userId, @RequestBody UpdateUserStatusRequest request) {
    User user = userService.getUserById(userId);
    if (request.getActive() == null && request.getBanned() == null) {
      throw new DomainException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
    }
    if (request.getActive() != null) user.setActive(request.getActive());
    if (request.getBanned() != null) user.setBanned(request.getBanned());
    userRepository.save(user);
    return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái người dùng thành công"));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công"));
  }
}
