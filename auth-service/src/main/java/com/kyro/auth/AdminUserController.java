package com.kyro.auth;

import com.kyro.auth.dto.BasicUserDTO;
import com.kyro.auth.dto.ChangeRoleRequest;
import com.kyro.auth.dto.UpdateUserRequest;
import com.kyro.auth.dto.UpdateUserStatusRequest;
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
      return ResponseEntity.badRequest().body(Map.of("message", "Trạng thái không hợp lệ"));
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
