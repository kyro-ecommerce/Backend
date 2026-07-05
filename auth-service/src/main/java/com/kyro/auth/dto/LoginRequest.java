package com.kyro.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

  @NotBlank(message = "Email can't be blank")
  @Email(message = "Email is not valid")
  private String email;

  @NotBlank(message = "Password can't be blank")
  private String password;
}
