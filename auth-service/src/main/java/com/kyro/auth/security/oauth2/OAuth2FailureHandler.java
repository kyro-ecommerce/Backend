package com.kyro.auth.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OAuth2FailureHandler.class);

  @Value("${app.oauth2.failureRedirectUri:http://localhost:5173/oauth2/callback}")
  private String defaultFailureRedirectUri;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {

    log.error("OAuth2 authentication failed: {}", exception.getMessage());

    String errorMessage = exception.getMessage();
    if (errorMessage == null || errorMessage.isEmpty()) {
      errorMessage = "Đăng nhập thất bại, vui lòng thử lại.";
    }

    String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

    String redirectUrl =
        UriComponentsBuilder.fromUriString(defaultFailureRedirectUri)
            .queryParam("error", encodedErrorMessage)
            .build()
            .toUriString();

    log.info("Redirecting to: {}", redirectUrl);

    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }
}
