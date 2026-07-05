package com.kyro.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
Cookie is a small piece of data sent from a web server to the user's browser, stored on the user's machine, and sent back with subsequent requests.


*/
@Component
public class CookieUtils {
  private static final Logger log = LoggerFactory.getLogger(CookieUtils.class);

  @Value("${app.useSecureCookie}")
  private boolean useSecureCookie;

  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  /**
   * Create and add refresh token cookie with HttpOnly, Secure, SameSite properties to HTTP response
   */
  public void addRefreshTokenCookie(
      HttpServletResponse response, String refreshToken, long maxAge) {
    if (response == null) {
      throw new IllegalArgumentException("HttpServletResponse cannot be null");
    }
    Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    //        Prevent JavaScript access
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge((int) (maxAge / 1000));
    refreshTokenCookie.setSecure(useSecureCookie);
    String sameSite = useSecureCookie ? "None" : "Lax";
    setResponseHeader(response, refreshTokenCookie, sameSite);
  }

  /** Set Set-Cookie header with security attributes */
  private void setResponseHeader(
      HttpServletResponse response, Cookie refreshTokenCookie, String sameSite) {
    StringBuilder cookieHeader = new StringBuilder();
    cookieHeader
        .append(refreshTokenCookie.getName())
        .append("=")
        .append(refreshTokenCookie.getValue())
        .append("; HttpOnly; Path=")
        .append(refreshTokenCookie.getPath())
        .append("; Max-Age=")
        .append(refreshTokenCookie.getMaxAge())
        .append(useSecureCookie ? "; Secure" : "")
        .append("; SameSite=")
        .append(sameSite);
    response.setHeader("Set-Cookie", cookieHeader.toString());
  }

  /** Extract refresh token from cookie */
  public String getRefreshTokenFromCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        System.out.println("Names of the cookie found: " + cookie.getName());
        if ("refreshToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  /** Remove refresh token cookie */
  public void deleteRefreshTokenCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0); // Delete cookie
    response.addCookie(cookie);
  }

  /** Log all cookies for debugging */
  public void logCookies(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      log.info("Cookies from request:");
      for (Cookie cookie : cookies) {
        log.info("Cookie name: {}, value: {}", cookie.getName(), cookie.getValue());
      }
    } else {
      log.info("No cookies found in request");
    }
  }
}

/*
Manages refresh token for JWT authentication, allowing tokens to be refreshed without logging in again. Focuses on security with HttpOnly, Secure, and SameSite. Useful in stateless authentication systems.
 */
