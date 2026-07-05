package com.kyro.auth.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
  private String firstName;
  private String lastName;
  private String mobile;
}
