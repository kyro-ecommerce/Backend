package com.kyro.auth.dto;

public class UpdateUserStatusRequest {
  private Boolean active;
  private Boolean banned;

  public UpdateUserStatusRequest() {}

  public UpdateUserStatusRequest(Boolean active, Boolean banned) {
    this.active = active;
    this.banned = banned;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public Boolean getBanned() {
    return banned;
  }

  public void setBanned(Boolean banned) {
    this.banned = banned;
  }
}
