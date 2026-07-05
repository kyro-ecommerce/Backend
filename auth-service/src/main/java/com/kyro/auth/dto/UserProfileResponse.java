package com.kyro.auth.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class UserProfileResponse {
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private String mobile;
  private String role;
  private Boolean status;
  private List<AddressDTO> address = new ArrayList<>();
  private LocalDateTime createdAt;
  private String imageUrl;
  private String oauthProvider;
}
