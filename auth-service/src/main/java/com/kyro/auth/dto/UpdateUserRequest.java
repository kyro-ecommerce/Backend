package com.kyro.auth.dto;

public class UpdateUserRequest {
  private String firstName;
  private String lastName;
  private String phoneNumber;

  public UpdateUserRequest() {}

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
