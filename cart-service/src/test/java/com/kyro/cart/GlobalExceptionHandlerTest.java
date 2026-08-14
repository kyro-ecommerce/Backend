package com.kyro.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
