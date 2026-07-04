package com.kyro.admin;

import com.kyro.auth.dto.ChangeRoleRequest;
import com.kyro.auth.dto.UpdateUserInfoRequest;
import com.kyro.auth.dto.UpdateUserStatusRequest;
import com.kyro.auth.dto.UserDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/users")
public class AdminManageUserController {

    private final AdminManageUserService adminUserService;

    @GetMapping("/all")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {

        Page<UserDTO> users = adminUserService.getAllUsers(page, size, search, role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserDetails(@PathVariable Long userId) {
        UserDTO user = adminUserService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody UpdateUserInfoRequest request) {
        UserDTO updatedUser = adminUserService.updateUserInfo(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/change-role")
    public ResponseEntity<UserDTO> changeUserRole(
            @PathVariable Long userId,
            @RequestBody ChangeRoleRequest request) {

        UserDTO updatedUser = adminUserService.changeUserRole(userId, request.getRole());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request) {

        UserDTO updatedUser = adminUserService.updateUserStatus(userId, request.isActive());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "Delete user success"));
    }

    @GetMapping("/customers/stats")
    public ResponseEntity<Map<String, Object>> getCustomerStats() {
        Map<String, Object> stats = adminUserService.getCustomerStatistics();
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{userId}/ban")
    public ResponseEntity<UserDTO> banUser(
            @PathVariable Long userId,
            @RequestParam boolean banned) {
        UserDTO updatedUser = adminUserService.banUser(userId, banned);
        return ResponseEntity.ok(updatedUser);
    }
}
