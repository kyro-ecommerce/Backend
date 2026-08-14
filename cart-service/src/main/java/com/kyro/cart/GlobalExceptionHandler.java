package com.kyro.cart;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final String BASE_TYPE_URI = "https://api.kyro.com/errors/";

  @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail handleResponseStatus(ResponseStatusException ex) {
    String code = codeFor(ex.getStatusCode());
    return problem(ex.getStatusCode(), code, ex.getReason());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail problem =
        problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dữ liệu không hợp lệ.");
    List<Map<String, String>> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    Map.of(
                        "field",
                        error.getField(),
                        "message",
                        String.valueOf(error.getDefaultMessage())))
            .toList();
    problem.setProperty("errors", errors);
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    LOGGER.error("Unhandled API exception in cart-service", ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Đã xảy ra lỗi nội bộ.");
  }

  private ProblemDetail problem(HttpStatusCode status, String code, String detail) {
    String message = detail == null || detail.isBlank() ? "Yêu cầu không thể xử lý." : detail;
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
    problem.setType(URI.create(BASE_TYPE_URI + code.toLowerCase(Locale.ENGLISH).replace('_', '-')));
    problem.setTitle(
        Arrays.stream(code.split("_"))
            .map(
                word ->
                    Character.toUpperCase(word.charAt(0))
                        + word.substring(1).toLowerCase(Locale.ENGLISH))
            .reduce((left, right) -> left + " " + right)
            .orElse(code));
    problem.setProperty("code", code);
    problem.setProperty("message", message);
    return problem;
  }

  private String codeFor(HttpStatusCode status) {
    return switch (status.value()) {
      case 400 -> "INVALID_ARGUMENT";
      case 404 -> "RESOURCE_NOT_FOUND";
      case 409 -> "CART_CONFLICT";
      case 503 -> "DEPENDENCY_UNAVAILABLE";
      default -> status.is5xxServerError() ? "INTERNAL_ERROR" : "REQUEST_FAILED";
    };
  }
}
