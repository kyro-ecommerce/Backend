package com.kyro.auth.dto;

public class UpdateUserStatusRequest {
  private boolean active;

  public UpdateUserStatusRequest() {}

  public UpdateUserStatusRequest(boolean active) {
    this.active = active;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
