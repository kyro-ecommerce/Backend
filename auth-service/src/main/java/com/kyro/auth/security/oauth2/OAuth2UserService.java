package com.kyro.auth.security.oauth2;

import com.kyro.auth.Role;
import com.kyro.auth.RoleRepository;
import com.kyro.auth.User;
import com.kyro.auth.UserRepository;
import com.kyro.auth.security.userdetails.AppUserDetails;
import com.kyro.enums.UserRole;
import java.util.List; // Add import
import java.util.Map;
import java.util.Optional; // Add import
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference; // Add import
import org.springframework.http.HttpEntity; // Add import
import org.springframework.http.HttpHeaders; // Add import
import org.springframework.http.HttpMethod; // Add import
import org.springframework.http.ResponseEntity; // Add import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken; // Add import
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final RestTemplate restTemplate;

  private static final String OAUTH2_PROCESSING_ERROR_CODE = "oauth2_processing_error";
  private static final String EMAIL_NOT_FOUND_ERROR_CODE = "email_not_found";
  private static final String ROLE_NOT_FOUND_ERROR_CODE = "role_not_found";
  private static final String GITHUB_EMAILS_API_URL = "https://api.github.com/user/emails";

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = super.loadUser(userRequest);

    try {
      return processOAuth2User(userRequest, oauth2User);
    } catch (OAuth2AuthenticationException ex) {
      throw ex;
    } catch (Exception ex) {
      log.error(
          "Unexpected error during OAuth2 user processing for provider {}: {}",
          userRequest.getClientRegistration().getRegistrationId(),
          ex.getMessage(),
          ex);
      OAuth2Error error =
          new OAuth2Error(
              OAUTH2_PROCESSING_ERROR_CODE,
              "An unexpected error occurred processing the OAuth2 user: " + ex.getMessage(),
              null);
      throw new OAuth2AuthenticationException(error, ex.getMessage(), ex);
    }
  }

  private AppUserDetails processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
    String provider = userRequest.getClientRegistration().getRegistrationId();
    String email = extractEmail(oauth2User.getAttributes(), provider, userRequest);

    if (!StringUtils.hasText(email)) {
      log.error("Email not found from OAuth2 provider: {}", provider);
      OAuth2Error error =
          new OAuth2Error(
              EMAIL_NOT_FOUND_ERROR_CODE,
              "Could not get email from OAuth2 provider ("
                  + provider
                  + "). Check provider configuration or user's email settings.",
              null);
      throw new OAuth2AuthenticationException(error, "Email not found from OAuth2 provider");
    }

    User user = userRepository.findByEmail(email);

    if (user == null) {
      user = this.createUser(oauth2User.getAttributes(), email, provider);
      log.info("Created new user from OAuth2 provider {}: {}", provider, email);
    } else {
      log.info("Existing user logged in via OAuth2 provider {}: {}", provider, email);
      if (!user.isActive()) {
        user.setActive(true);
        userRepository.save(user);
        log.info("Activated existing inactive user: {}", email);
      }
    }

    return AppUserDetails.buildUserDetails(user);
  }

  private String extractEmail(
      Map<String, Object> attributes, String provider, OAuth2UserRequest userRequest) {
    String email = null;

    if ("google".equalsIgnoreCase(provider)) {
      email = (String) attributes.get("email");
    } else if ("github".equalsIgnoreCase(provider)) {
      email = (String) attributes.get("email");

      // Call /user/emails API if email is missing from main attributes for GitHub
      if (!StringUtils.hasText(email)) {
        log.warn(
            "Primary email attribute is null/empty for GitHub user [ID: {}]. Attempting to fetch"
                + " from /user/emails endpoint.",
            attributes.get("id"));
        email = fetchGithubPrimaryVerifiedEmail(userRequest.getAccessToken());
      }
    }

    return email;
  }

  private String fetchGithubPrimaryVerifiedEmail(OAuth2AccessToken accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken.getTokenValue());
    headers.add("Accept", "application/vnd.github.v3+json");

    HttpEntity<String> entity = new HttpEntity<>("", headers);

    try {
      ResponseEntity<List<Map<String, Object>>> response =
          restTemplate.exchange(
              GITHUB_EMAILS_API_URL,
              HttpMethod.GET,
              entity,
              new ParameterizedTypeReference<List<Map<String, Object>>>() {});

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        List<Map<String, Object>> emails = response.getBody();
        Optional<String> primaryVerifiedEmail =
            emails.stream()
                .filter(
                    emailMap ->
                        Boolean.TRUE.equals(emailMap.get("primary"))
                            && Boolean.TRUE.equals(emailMap.get("verified")))
                .map(emailMap -> (String) emailMap.get("email"))
                .findFirst();

        if (primaryVerifiedEmail.isPresent()) {
          log.info(
              "Successfully fetched primary verified email from GitHub /user/emails endpoint.");
          return primaryVerifiedEmail.get();
        } else {
          log.warn(
              "Could not find a primary and verified email in the response from GitHub"
                  + " /user/emails.");
          return null;
        }
      } else {
        log.error(
            "Failed to fetch emails from GitHub /user/emails. Status code: {}",
            response.getStatusCode());
        return null;
      }
    } catch (Exception e) {
      log.error("Error calling GitHub /user/emails endpoint: {}", e.getMessage(), e);
      return null;
    }
  }

  private User createUser(Map<String, Object> attributes, String email, String provider) {
    User user = new User();
    user.setEmail(email);
    user.setActive(true);

    String name = null;
    String imageUrl = null;
    String providerId = null;

    if ("google".equalsIgnoreCase(provider)) {
      name = (String) attributes.get("name");
      imageUrl = (String) attributes.get("picture");
      providerId = (String) attributes.get("sub");
    } else if ("github".equalsIgnoreCase(provider)) {
      name = (String) attributes.get("name");
      if (!StringUtils.hasText(name)) {
        name = (String) attributes.get("login");
      }
      imageUrl = (String) attributes.get("avatar_url");
      Object idObj = attributes.get("id");
      if (idObj != null) {
        providerId = String.valueOf(idObj);
      }
    }

    setNames(user, name);
    user.setImageUrl(imageUrl);
    user.setOauthProvider(provider);
    user.setOauthProviderId(providerId);

    // Generate a secure random password (user won't use it for OAuth login)
    String randomPassword = UUID.randomUUID().toString();
    user.setPassword(passwordEncoder.encode(randomPassword));

    // Assign default role
    Role role =
        roleRepository
            .findByName(UserRole.CUSTOMER)
            .orElseThrow(
                () -> {
                  log.error("Default role CUSTOMER not found in database!");
                  OAuth2Error error =
                      new OAuth2Error(
                          ROLE_NOT_FOUND_ERROR_CODE,
                          "Default user role (CUSTOMER) could not be found.",
                          null);
                  return new RuntimeException("Default role not found");
                });
    user.setRole(role);

    return userRepository.save(user);
  }

  private void setNames(User user, String fullName) {
    if (StringUtils.hasText(fullName)) {
      String[] names = fullName.trim().split("\\s+");
      if (names.length > 0) {
        user.setFirstName(names[0]);
        if (names.length > 1) {
          StringBuilder lastName = new StringBuilder();
          for (int i = 1; i < names.length; i++) {
            lastName.append(names[i]).append(" ");
          }
          user.setLastName(lastName.toString().trim());
        } else {
          user.setLastName("");
        }
      }
    } else {
      user.setFirstName("User");
      user.setLastName(String.valueOf(System.currentTimeMillis()));
    }
  }
}
