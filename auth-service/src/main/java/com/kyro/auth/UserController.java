package com.kyro.auth;

import com.kyro.auth.dto.AddAddressRequest;
import com.kyro.auth.dto.AddressDTO;
import com.kyro.auth.dto.BasicUserDTO;
import com.kyro.auth.dto.ChangeRoleRequest;
import com.kyro.auth.dto.UpdateUserRequest;
import com.kyro.auth.dto.UserProfileResponse;
import com.kyro.exceptions.DomainException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
@Slf4j
public class UserController {
  private final UserService userService;
  private final UserRepository userRepository;

  @PutMapping("/update")
  public ResponseEntity<Map<String, String>> updateUser(
      @RequestBody UpdateUserRequest request, @RequestHeader("Authorization") String jwt) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      throw new DomainException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    User userTemp = userService.findUserByJwt(jwt);
    userService.updateUser(request, userTemp.getId());
    return ResponseEntity.ok(Map.of("message", "Update User Success!"));
  }

  @DeleteMapping("/delete/{userId}")
  public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(Map.of("message", "Delete User Success!"));
  }

  @Transactional
  @GetMapping("/profile")
  public ResponseEntity<UserProfileResponse> getUserProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      log.error("Authentication failed - auth object is null or name is null");
      throw new DomainException(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }

    String email = authentication.getName();
    log.info("Getting profile for user with email: {}", email);

    User user = userRepository.findByEmail(email);
    if (user == null) {
      log.error("User not found for email: {}", email);
      throw new DomainException(HttpStatus.NOT_FOUND, "User not found for email: " + email);
    }

    log.info("User found with ID: {}", user.getId());

    List<AddressDTO> addressDTOS = new ArrayList<>();
    if (user.getAddress() != null) {
      for (Address address : user.getAddress()) {
        if (address != null) {
          addressDTOS.add(new AddressDTO(address));
        }
      }
    }

    UserProfileResponse profileResponse = new UserProfileResponse();

    if (authentication.isAuthenticated()) {
      log.info("User is authenticated");
      profileResponse.setStatus(true);
    } else {
      log.warn("User is not authenticated");
      profileResponse.setStatus(false);
    }

    profileResponse.setId(user.getId());
    profileResponse.setEmail(user.getEmail());
    profileResponse.setFirstName(user.getFirstName());
    profileResponse.setLastName(user.getLastName());
    profileResponse.setMobile(user.getPhone());
    profileResponse.setRole(
        user.getRole() != null && user.getRole().getName() != null
            ? user.getRole().getName().name()
            : "UNKNOWN");
    profileResponse.setAddress(addressDTOS);
    profileResponse.setCreatedAt(user.getCreatedAt());
    profileResponse.setImageUrl(user.getImageUrl());
    profileResponse.setOauthProvider(user.getOauthProvider());

    log.info("Successfully retrieved profile for user: {}", email);
    return ResponseEntity.ok(profileResponse);
  }

  @Transactional
  @GetMapping("/address")
  public ResponseEntity<List<AddressDTO>> getUserAddress() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new DomainException(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }

    String email = authentication.getName();
    User user = userRepository.findByEmail(email);

    if (user == null) {
      throw new DomainException(HttpStatus.NOT_FOUND, "User not found for email: " + email);
    }

    List<AddressDTO> addressDTOS = new ArrayList<>();

    if (user.getAddress() != null) {
      for (Address a : user.getAddress()) {
        addressDTOS.add(new AddressDTO(a));
      }
    }

    return ResponseEntity.ok(addressDTOS);
  }

  @PostMapping("/addresses")
  @Transactional
  public ResponseEntity<AddressDTO> addUserAddress(
      @RequestHeader("Authorization") String jwt, @RequestBody AddAddressRequest req) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getName() == null) {
      throw new DomainException(HttpStatus.UNAUTHORIZED, "Authentication failed");
    }
    String email = authentication.getName();
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new DomainException(HttpStatus.NOT_FOUND, "User not found for email: " + email);
    }

    AddressDTO createdAddress = userService.addUserAddress(user, req);
    return ResponseEntity.ok(createdAddress);
  }

  @PostMapping("/change-role")
  public ResponseEntity<BasicUserDTO> changeUserRole(
      @RequestHeader("Authorization") String jwt, @RequestBody ChangeRoleRequest request) {

    User user = userService.findUserByJwt(jwt);

    String targetRole = request.getRole().toUpperCase();
    if (!targetRole.equals("CUSTOMER") && !targetRole.equals("SELLER")) {
      throw new IllegalArgumentException("Invalid role. Only CUSTOMER or SELLER are supported");
    }

    if (user.getRole().getName().name().equals(targetRole)) {
      throw new IllegalArgumentException("Account already has role " + targetRole);
    }

    BasicUserDTO updatedUser = userService.changeUserRole(user.getId(), targetRole);
    return ResponseEntity.ok(updatedUser);
  }

  @GetMapping("/internal/{userId}")
  public ResponseEntity<BasicUserDTO> getUserByIdInternal(@PathVariable Long userId) {
    User user = userService.getUserById(userId);
    return ResponseEntity.ok(userService.convertToBasicDto(user));
  }

  @GetMapping("/internal/address/{addressId}")
  @Transactional
  public ResponseEntity<Address> getAddressByIdInternal(
      @PathVariable Long addressId, @RequestParam Long userId) {
    User user = userService.getUserById(userId);
    Address address =
        user.getAddress().stream()
            .filter(a -> a.getId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Address not found"));
    return ResponseEntity.ok(address);
  }
}
