package com.kyro.auth.dto;

public class UpdateUserInfoRequest {
  private String firstName;
  private String lastName;
  private String mobile;

  public UpdateUserInfoRequest() {}

  public UpdateUserInfoRequest(String firstName, String lastName, String mobile) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.mobile = mobile;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }
}
