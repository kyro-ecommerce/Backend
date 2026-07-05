package com.kyro.auth.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BasicUserDTO {
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private String mobile;
  private boolean active;
  private String role;
  private LocalDateTime createdAt;
  private String imageUrl;
  private String oauthProvider;
}
