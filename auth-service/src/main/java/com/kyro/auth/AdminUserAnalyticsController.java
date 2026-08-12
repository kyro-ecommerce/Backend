package com.kyro.auth;

import com.kyro.enums.UserRole;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix:/api/v1}/admin/analytics/users")
public class AdminUserAnalyticsController {
  private final UserRepository userRepository;

  public AdminUserAnalyticsController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/summary")
  public ResponseEntity<Map<String, Object>> getSummary() {
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
}
