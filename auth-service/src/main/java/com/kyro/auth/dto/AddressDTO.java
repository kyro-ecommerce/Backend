package com.kyro.auth.dto;

import com.kyro.auth.Address;

public class AddressDTO {
  private Long id;
  private String fullName;
  private String phoneNumber;
  private String province;
  private String district;
  private String ward;
  private String street;
  private String note;

  public AddressDTO() {}

  public AddressDTO(Address address) {
    if (address != null) {
      this.id = address.getId();
      this.fullName = address.getFullName();
      this.phoneNumber = address.getPhoneNumber();
      this.province = address.getProvince();
      this.district = address.getDistrict();
      this.ward = address.getWard();
      this.street = address.getStreet();
      this.note = address.getNote();
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getWard() {
    return ward;
  }

  public void setWard(String ward) {
    this.ward = ward;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
