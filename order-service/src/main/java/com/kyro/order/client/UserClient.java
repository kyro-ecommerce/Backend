package com.kyro.order.client;

import lombok.Data;
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

  @Data
  class AddressResponse {
    private Long id;
    private String fullName;
    private String street;
    private String ward;
    private String district;
    private String province;
    private String phoneNumber;
    private String note;
  }
}
