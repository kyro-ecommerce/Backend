package com.kyro.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Feign client to communicate with Auth Service for user details and addresses. */
@FeignClient(name = "auth-service", fallback = UserClientFallback.class)
public interface UserClient {

  @GetMapping("/api/v1/internal/users/{userId}/addresses/{addressId}")
  AddressResponse getAddressById(
      @PathVariable("userId") Long userId, @PathVariable("addressId") Long addressId);

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

@Component
class UserClientFallback implements UserClient {
  @Override
  public AddressResponse getAddressById(Long addressId, Long userId) {
    return new AddressResponse(
        addressId,
        "Khách hàng",
        "N/A",
        "N/A",
        "N/A",
        "N/A",
        "0000000000",
        "Địa chỉ lưu từ fallback khi service phản hồi chậm");
  }
}
