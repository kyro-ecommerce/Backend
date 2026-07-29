package com.kyro.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address")
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Full name is required")
  @Size(max = 50, message = "Full name must be less than 50 characters")
  @Column(name = "full_name")
  private String fullName;

  @NotBlank(message = "Province is required")
  @Size(max = 100, message = "Province must be less than 100 characters")
  @Column(name = "province")
  private String province;

  @NotBlank(message = "district is required")
  @Size(max = 50, message = "district must be less than 50 characters")
  @Column(name = "district")
  private String district;

  @NotBlank(message = "ward is required")
  @Size(max = 50, message = "ward must be less than 50 characters")
  @Column(name = "ward")
  private String ward;

  @NotBlank(message = "street is required")
  @Size(max = 50, message = "street must be less than 50 characters")
  @Column(name = "street")
  private String street;

  @Size(max = 100, message = "note must be less than 100 characters")
  @Column(name = "note")
  private String note;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @NotBlank(message = "Mobile number is required")
  @Size(max = 15, message = "Mobile number must be less than 15 characters")
  @Column(name = "phone_number")
  private String phoneNumber;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getProvince() { return province; }
  public void setProvince(String province) { this.province = province; }
  public String getDistrict() { return district; }
  public void setDistrict(String district) { this.district = district; }
  public String getWard() { return ward; }
  public void setWard(String ward) { this.ward = ward; }
  public String getStreet() { return street; }
  public void setStreet(String street) { this.street = street; }
  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public String getPhoneNumber() { return phoneNumber; }
  public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
