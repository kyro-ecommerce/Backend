package com.kyro.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

class ErrorResponseUtilsTest {
  @Test
  void writesAuthenticationErrorsAsProblemDetail() throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();

    new ErrorResponseUtils(new ObjectMapper())
        .sendAuthenticationError(response, "Sign in required");

    assertEquals(401, response.getStatus());
    assertEquals(MediaType.APPLICATION_PROBLEM_JSON_VALUE, response.getContentType());
    String body = response.getContentAsString();
    assertTrue(body.contains("UNAUTHORIZED"));
    assertTrue(body.contains("\"status\":401"));
    assertTrue(body.contains("\"message\""));
  }
}
