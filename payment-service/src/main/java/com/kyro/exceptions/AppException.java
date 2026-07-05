package com.kyro.exceptions;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
  private final HttpStatus status;
  private final String errorCode;

  public AppException(String message) {
    super(message);
    this.status = HttpStatus.BAD_REQUEST;
    this.errorCode = "APP_ERROR";
  }

  public AppException(String message, String errorCode) {
    super(message);
    this.status = HttpStatus.BAD_REQUEST;
    this.errorCode = errorCode;
  }

  public AppException(HttpStatus status, String errorCode, String message) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  // Compatibility method
  public String getCode() {
    return errorCode;
  }
}
