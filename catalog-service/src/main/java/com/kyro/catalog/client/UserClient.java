package com.kyro.catalog.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Feign client to communicate with the Auth Service. */
@FeignClient(name = "auth-service")
public interface UserClient {

  /**
   * Fetches basic user information by user ID.
   *
   * @param userId user ID
   * @return UserResponse containing user names
   */
  @GetMapping("/api/v1/internal/users/{userId}")
  UserResponse getUserById(@PathVariable("userId") Long userId);

  @JsonIgnoreProperties(ignoreUnknown = true)
  record UserResponse(Long id, String email, String firstName, String lastName) {}
}
