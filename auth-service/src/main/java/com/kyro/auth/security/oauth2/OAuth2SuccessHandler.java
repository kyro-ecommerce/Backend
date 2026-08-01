package com.kyro.auth.security.oauth2;

import com.kyro.auth.security.jwt.JwtUtils;
import com.kyro.auth.security.userdetails.AppUserDetails;
import com.kyro.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  private static final Logger log = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
  private final JwtUtils jwtUtils;
  private final CookieUtils cookieUtils;

  public OAuth2SuccessHandler(JwtUtils jwtUtils, CookieUtils cookieUtils) {
    this.jwtUtils = jwtUtils;
    this.cookieUtils = cookieUtils;
  }

  @Value("${app.oauth2.redirectUri:http://localhost:5173/oauth2/callback}")
  private String defaultRedirectUri;

  @Value("${app.oauth2.failureRedirectUri:http://localhost:5173/oauth2/callback}")
  private String defaultFailureRedirectUri;

  @Value("${auth.token.refreshExpirationInMils}")
  private Long refreshTokenExpirationTime;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    if (!(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
      log.error(
          "Principal is not an instance of AppUserDetails. Actual type: {}",
          authentication.getPrincipal().getClass().getName());
      sendErrorRedirect(
          request, response, "invalid_principal_type", "Internal server error during login.");
      return;
    }

    String email = userDetails.getEmail();

    if (email == null || email.isEmpty()) {
      log.error(
          "Email is null or empty in AppUserDetails for principal: {}", userDetails.getUsername());
      sendErrorRedirect(
          request,
          response,
          "email_extraction_failed",
          "Could not determine user email after login.");
      return;
    }

    String accessToken = jwtUtils.generateAccessToken(authentication);
    String refreshToken = jwtUtils.generateRefreshToken(email);

    cookieUtils.addRefreshTokenCookie(response, refreshToken, refreshTokenExpirationTime);

    String redirectUrl = buildRedirectUrl(accessToken);
    log.info("OAuth2 login successful for user {}, redirecting to: {}", email, redirectUrl);
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }

  private String buildRedirectUrl(String accessToken) {
    String baseUri = (defaultRedirectUri != null) ? defaultRedirectUri : "/";
    return UriComponentsBuilder.fromUriString(baseUri)
        .queryParam("token", accessToken)
        .build()
        .toUriString();
  }

  private void sendErrorRedirect(
      HttpServletRequest request,
      HttpServletResponse response,
      String errorCode,
      String defaultMessage)
      throws IOException {
    String errorMessage = defaultMessage + " (Code: " + errorCode + ")";
    log.error("OAuth2 Success Handler Error - Redirecting with error: {}", errorMessage);

    String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    String redirectUrl =
        UriComponentsBuilder.fromUriString(defaultFailureRedirectUri)
            .queryParam("error", encodedErrorMessage)
            .build()
            .toUriString();

    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }
}
