package com.kyro.auth.dto;

import lombok.Data;

@Data
public class AddAddressRequest {
  private String fullName;
  private String phoneNumber;
  private String province;
  private String district;
  private String ward;
  private String street;
  private String note;
}
