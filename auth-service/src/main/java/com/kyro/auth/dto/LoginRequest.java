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

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
}
