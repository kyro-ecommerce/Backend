package com.kyro.exceptions;

import java.util.List;
import org.springframework.http.HttpStatus;

public class ValidationException extends AppException {
  private final List<FieldViolation> violations;

  public ValidationException(List<FieldViolation> violations) {
    super(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed");
    this.violations = List.copyOf(violations);
  }

  public List<FieldViolation> getViolations() {
    return violations;
  }

  public record FieldViolation(String field, String code, String message) {}
}
