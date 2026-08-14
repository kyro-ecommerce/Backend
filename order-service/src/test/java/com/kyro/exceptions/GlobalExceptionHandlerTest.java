package com.kyro.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

class GlobalExceptionHandlerTest {
  @Test
  void mapsInvalidRequestWithoutFallingThroughTo500() {
    assertProblem(
        new GlobalExceptionHandler().handleInvalidRequest(new Exception()),
        400,
        "INVALID_ARGUMENT");
  }

  private static void assertProblem(ProblemDetail problem, int status, String code) {
    assertEquals(status, problem.getStatus());
    assertNotNull(problem.getType());
    assertNotNull(problem.getTitle());
    assertNotNull(problem.getDetail());
    assertEquals(code, problem.getProperties().get("code"));
    assertEquals(problem.getDetail(), problem.getProperties().get("message"));
  }
}
