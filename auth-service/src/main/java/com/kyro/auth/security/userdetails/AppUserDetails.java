package com.kyro.auth.security.userdetails;

import com.kyro.auth.User;
import java.util.Collection;
import java.util.List;
import java.util.Map; // ** Add import **
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User; // ** Add import **

public class AppUserDetails implements UserDetails, OAuth2User {

  private final Long id;
  private final String email;
  private final String password;
  private final boolean enabled;
  private final Collection<? extends GrantedAuthority> authorities;

  private Map<String, Object> attributes;

  public AppUserDetails(
      Long id,
      String email,
      String password,
      boolean enabled,
      Collection<? extends GrantedAuthority> authorities,
      Map<String, Object> attributes) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.enabled = enabled;
    this.authorities = authorities;
    this.attributes = attributes;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public static AppUserDetails buildUserDetails(User user) {
    List<GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority(user.getRole().getName().name()));
    return new AppUserDetails(
        user.getId(), user.getEmail(), user.getPassword(), user.isActive(), authorities, null);
  }

  public static AppUserDetails buildOAuth2UserDetails(User user, Map<String, Object> attributes) {
    List<GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority(user.getRole().getName().name()));
    return new AppUserDetails(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.isActive(),
        authorities,
        attributes);
  }

  // --- Methods from UserDetails ---
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email; // Use email as username
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  // --- Methods from OAuth2User ---
  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getName() {
    return String.valueOf(this.id);
  }
}
