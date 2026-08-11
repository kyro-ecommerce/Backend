package com.kyro.auth;

import com.kyro.auth.dto.BasicUserDTO;
import com.kyro.auth.dto.UpdateUserRequest;
import com.kyro.enums.UserRole;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
      @RequestParam(required = false) String role) {

    Pageable pageable = PageRequest.of(page, size);
    String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    UserRole userRoleEnum = null;
    if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("all")) {
      try {
        userRoleEnum = UserRole.valueOf(role.trim().toUpperCase());
      } catch (IllegalArgumentException ignored) {
      }
    }

    Page<User> usersPage =
        userRepository.findAdminUsersWithFilters(cleanSearch, userRoleEnum, pageable);
    Page<BasicUserDTO> dtoList = usersPage.map(userService::convertToBasicDto);

    return ResponseEntity.ok(dtoList);
  }

  @GetMapping("/customers/stats")
  public ResponseEntity<Map<String, Object>> getCustomerStats() {
    long totalCustomers = userRepository.countByRoleName(UserRole.CUSTOMER);
    long totalAdmins = userRepository.countByRoleName(UserRole.ADMIN);
    long totalUsers = userRepository.count();

    return ResponseEntity.ok(
        Map.of(
            "totalCustomers", totalCustomers,
            "totalAdmins", totalAdmins,
            "totalUsers", totalUsers,
            "activeCustomers", totalCustomers));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<BasicUserDTO> getUserDetails(@PathVariable Long userId) {
    User user = userService.getUserById(userId);
    return ResponseEntity.ok(userService.convertToBasicDto(user));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<BasicUserDTO> updateUser(
      @PathVariable Long userId, @RequestBody UpdateUserRequest request) {
    userService.updateUser(request, userId);
    return ResponseEntity.ok(userService.convertToBasicDto(userService.getUserById(userId)));
  }

  @PutMapping("/{userId}/change-role")
  public ResponseEntity<BasicUserDTO> changeRole(
      @PathVariable Long userId, @RequestBody Map<String, String> body) {
    String role = body.get("role");
    BasicUserDTO updated = userService.changeUserRole(userId, role);
    return ResponseEntity.ok(updated);
  }

  @PutMapping("/{userId}/status")
  public ResponseEntity<Map<String, String>> changeStatus(
      @PathVariable Long userId, @RequestBody Map<String, Boolean> body) {
    User user = userService.getUserById(userId);
    Boolean active = body.get("active");
    if (active != null) {
      user.setActive(active);
      userRepository.save(user);
    }
    return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái người dùng thành công"));
  }

  @PutMapping("/{userId}/ban")
  public ResponseEntity<Map<String, String>> banUser(
      @PathVariable Long userId, @RequestParam boolean banned) {
    User user = userService.getUserById(userId);
    user.setBanned(banned);
    userRepository.save(user);
    return ResponseEntity.ok(
        Map.of("message", banned ? "Khóa tài khoản thành công" : "Mở khóa tài khoản thành công"));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công"));
  }
}
