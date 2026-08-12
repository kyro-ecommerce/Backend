package com.kyro.exceptions;

import com.kyro.catalog.CategoryInUseException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String BASE_TYPE_URI = "https://api.kyro.com/errors/";

  // 1. Handles general unhandled exceptions (500 Internal Server Error)
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpectedException(Exception exception) {
    LOGGER.error("Unhandled API exception in catalog-service", exception);

    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, GlobalErrorCode.INTERNAL_ERROR.getDefaultMessage());
    problem.setTitle("Internal Server Error");
    problem.setType(URI.create("urn:problem-type:internal-server-error"));
    problem.setProperty("code", GlobalErrorCode.INTERNAL_ERROR.getCode());
    problem.setProperty("message", GlobalErrorCode.INTERNAL_ERROR.getDefaultMessage());
    return problem;
  }

  // 2. Handles custom AppException hierarchy
  @ExceptionHandler(AppException.class)
  public ProblemDetail handleAppException(AppException ex) {
    ProblemDetail problem = buildResponse(ex);
    if (ex instanceof CategoryInUseException categoryInUse) {
      problem.setProperty("blockedCategories", categoryInUse.getBlockedCategories());
    }
    return problem;
  }

  // 3. Handles Spring Validation annotations errors
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    List<ValidationException.FieldViolation> violations =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fe ->
                    new ValidationException.FieldViolation(
                        fe.getField(), fe.getCode(), fe.getDefaultMessage()))
            .toList();

    return buildResponse(new ValidationException(violations));
  }

  // 4. Handles JPA EntityNotFoundException
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex) {
    return buildResponse(new AppException(GlobalErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNoResourceFoundException(NoResourceFoundException ex) {
    return buildResponse(new AppException(GlobalErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()));
  }

  // 5. Handles JPA EntityExistsException
  @ExceptionHandler(EntityExistsException.class)
  public ProblemDetail handleEntityExistsException(EntityExistsException ex) {
    return buildResponse(
        new AppException(GlobalErrorCode.RESOURCE_ALREADY_EXISTS, ex.getMessage()));
  }

  // 6. Handles Database Constraint Violations
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    String detailMessage = "Database constraints violated";
    if (ex.getMostSpecificCause() != null) {
      detailMessage += ": " + ex.getMostSpecificCause().getMessage();
    }
    return buildResponse(new AppException(GlobalErrorCode.DATABASE_ERROR, detailMessage));
  }

  // 7. Handles Constraint Violation Exception
  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolationException(ConstraintViolationException ex) {
    return buildResponse(
        new AppException(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", ex.getMessage()));
  }

  // 8. Handles Illegal Argument Exception
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
    return buildResponse(new AppException(GlobalErrorCode.INVALID_ARGUMENT, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return buildResponse(
        new AppException(
            GlobalErrorCode.INVALID_ARGUMENT, "Invalid value for parameter: " + ex.getName()));
  }

  // Helper to build a standard ProblemDetail response
  private ProblemDetail buildResponse(AppException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(ex.getStatus());
    problem.setType(URI.create(BASE_TYPE_URI + toKebabCase(ex.getErrorCode())));
    problem.setTitle(toTitleCase(ex.getErrorCode()));
    problem.setDetail(ex.getMessage());
    problem.setProperty("code", ex.getErrorCode());
    problem.setProperty(
        "message", ex.getMessage()); // Ensures 100% backward compatibility with Frontend

    if (ex instanceof ValidationException ve && !ve.getViolations().isEmpty()) {
      problem.setProperty("errors", ve.getViolations());
    }

    logException(ex);
    return problem;
  }

  private void logException(AppException ex) {
    if (ex.getStatus().is5xxServerError()) {
      LOGGER.error(
          "Internal server error [{}] {}: {}",
          ex.getStatus().value(),
          ex.getErrorCode(),
          ex.getMessage(),
          ex);
    } else {
      LOGGER.warn(
          "Business error [{}] {}: {}", ex.getStatus().value(), ex.getErrorCode(), ex.getMessage());
    }
  }

  private String toKebabCase(String code) {
    return code.toLowerCase(Locale.ENGLISH).replace('_', '-');
  }

  private String toTitleCase(String code) {
    return Arrays.stream(code.split("_"))
        .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase(Locale.ENGLISH))
        .collect(Collectors.joining(" "));
  }
}
