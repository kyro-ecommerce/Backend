package com.kyro.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddAddressRequest {
  private String fullName;
  private String phoneNumber;
  private String email;
  private String province;
  private String district;
  private String ward;
  private String street;
  private String note;
}
