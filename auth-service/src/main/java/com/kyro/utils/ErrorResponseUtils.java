package com.kyro.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseUtils {
  private final ObjectMapper objectMapper; // To serialize object to JSON.

  public ErrorResponseUtils(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void sendAuthenticationError(HttpServletResponse response, String message)
      throws IOException {
    sendError(
        response, "UNAUTHORIZED", "Unauthorized", message, HttpServletResponse.SC_UNAUTHORIZED);
  }

  public void sendAccessDeniedError(HttpServletResponse response, String message)
      throws IOException {
    sendError(response, "FORBIDDEN", "Forbidden", message, HttpServletResponse.SC_FORBIDDEN);
  }

  private void sendError(
      HttpServletResponse response, String code, String title, String message, int status)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), message);
    problem.setType(
        URI.create(
            "https://api.kyro.com/errors/" + code.toLowerCase(Locale.ENGLISH).replace('_', '-')));
    problem.setTitle(title);
    problem.setProperty("code", code);
    problem.setProperty("message", message);
    objectMapper.writeValue(response.getOutputStream(), problem);
  }
}
