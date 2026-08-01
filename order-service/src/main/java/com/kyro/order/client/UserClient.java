package com.kyro.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Feign client to communicate with Auth Service for user details and addresses. */
@FeignClient(name = "auth-service")
public interface UserClient {

  @GetMapping("/api/v1/users/internal/address/{addressId}")
  AddressResponse getAddressById(
      @PathVariable("addressId") Long addressId, @RequestParam("userId") Long userId);

  record AddressResponse(
      Long id,
      String fullName,
      String street,
      String ward,
      String district,
      String province,
      String phoneNumber,
      String note) {}
}
