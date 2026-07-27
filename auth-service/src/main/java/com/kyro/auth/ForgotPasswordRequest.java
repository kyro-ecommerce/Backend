package com.kyro.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "OTP cannot be blank")
  private String otp;

  @NotBlank(message = "New password cannot be blank")
  @Size(min = 6, message = "New password must be at least 6 characters")
  private String newPassword;

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getOtp() { return otp; }
  public void setOtp(String otp) { this.otp = otp; }
  public String getNewPassword() { return newPassword; }
  public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
