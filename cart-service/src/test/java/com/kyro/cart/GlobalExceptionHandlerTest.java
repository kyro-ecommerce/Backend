package com.kyro.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {
  @Test
  void preservesConflictMessageInProblemDetail() {
    ProblemDetail problem =
        new GlobalExceptionHandler()
            .handleResponseStatus(
                new ResponseStatusException(HttpStatus.CONFLICT, "Biến thể không đủ tồn kho."));

    assertEquals(409, problem.getStatus());
    assertEquals("Biến thể không đủ tồn kho.", problem.getDetail());
    assertEquals("CART_CONFLICT", problem.getProperties().get("code"));
    assertEquals("Biến thể không đủ tồn kho.", problem.getProperties().get("message"));
  }

  @Test
  void mapsInvalidRequestWithoutFallingThroughTo500() {
    ProblemDetail problem = new GlobalExceptionHandler().handleInvalidRequest(new Exception());

    assertProblem(problem, 400, "INVALID_ARGUMENT");
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
