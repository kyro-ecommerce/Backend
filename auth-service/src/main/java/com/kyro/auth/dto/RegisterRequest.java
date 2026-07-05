package com.kyro.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank(message = "First name không được để trống")
  private String firstName;

  @NotBlank(message = "Last name không được để trống")
  private String lastName;

  @NotBlank(message = "Email không được để trống")
  @Email(message = "Email không hợp lệ")
  private String email;

  @NotBlank(message = "Password không được để trống")
  @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
  private String password;
}
