package com.kyro.auth.dto;

import com.kyro.auth.Address;
import com.kyro.auth.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String role;
  private String mobile;
  private boolean active;
  private boolean banned;
  private List<Address> addresses;
  private LocalDateTime createdAt;
  private String imageUrl;
  private String oauthProvider;
  private long orderCount;
  private BigDecimal totalSpent;

  // constructor to map all fields from User entity
  public UserDTO(User user) {
    this.id = user.getId();
    this.firstName = user.getFirstName();
    this.lastName = user.getLastName();
    this.email = user.getEmail();
    this.role = user.getRole() != null ? user.getRole().getName().toString() : null;
    this.mobile = user.getPhone();
    this.active = user.isActive();
    this.banned = user.isBanned();
    this.addresses = user.getAddress();
    this.createdAt = user.getCreatedAt();
    this.imageUrl = user.getImageUrl();
    this.oauthProvider = user.getOauthProvider();
    this.orderCount = 0;
    this.totalSpent = java.math.BigDecimal.ZERO;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public String getMobile() { return mobile; }
  public void setMobile(String mobile) { this.mobile = mobile; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public boolean isBanned() { return banned; }
  public void setBanned(boolean banned) { this.banned = banned; }
  public List<Address> getAddresses() { return addresses; }
  public void setAddresses(List<Address> addresses) { this.addresses = addresses; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public String getImageUrl() { return imageUrl; }
  public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  public String getOauthProvider() { return oauthProvider; }
  public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }
  public long getOrderCount() { return orderCount; }
  public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
  public BigDecimal getTotalSpent() { return totalSpent; }
  public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
}
