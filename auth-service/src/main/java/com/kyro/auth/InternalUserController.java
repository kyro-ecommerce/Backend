package com.kyro.auth;

import com.kyro.auth.dto.AddressDTO;
import com.kyro.auth.dto.BasicUserDTO;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/internal/users")
public class InternalUserController {
  private final UserService userService;

  public InternalUserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/{userId}")
  public ResponseEntity<BasicUserDTO> getUser(@PathVariable Long userId) {
    User user = userService.getUserById(userId);
    return ResponseEntity.ok(userService.convertToBasicDto(user));
  }

  @GetMapping("/{userId}/addresses/{addressId}")
  @Transactional
  public ResponseEntity<AddressDTO> getAddress(
      @PathVariable Long userId, @PathVariable Long addressId) {
    User user = userService.getUserById(userId);
    Address address =
        user.getAddress().stream()
            .filter(candidate -> candidate.getId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Address not found"));
    return ResponseEntity.ok(new AddressDTO(address));
  }
}
