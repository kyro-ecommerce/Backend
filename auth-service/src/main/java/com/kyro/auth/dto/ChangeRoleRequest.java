package com.kyro.auth.dto;

public class ChangeRoleRequest {
  private String role;

  public ChangeRoleRequest() {}

  public ChangeRoleRequest(String role) {
    this.role = role;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
