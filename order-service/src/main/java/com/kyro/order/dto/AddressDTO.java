package com.kyro.order.dto;

import com.kyro.order.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Address DTO for representing shipping address details in orders. */
@Data
@NoArgsConstructor
public class AddressDTO {
  private Long id;
  private String fullName;
  private String province;
  private String district;
  private String ward;
  private String street;
  private String note;
  private String phoneNumber;

  public AddressDTO(Address address) {
    if (address != null) {
      this.id = address.getId();
      this.fullName = address.getFullName();
      this.province = address.getProvince();
      this.district = address.getDistrict();
      this.ward = address.getWard();
      this.street = address.getStreet();
      this.note = address.getNote();
      this.phoneNumber = address.getPhoneNumber();
    }
  }
}
