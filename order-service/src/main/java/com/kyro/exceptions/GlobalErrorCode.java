package com.kyro.exceptions;

import org.springframework.http.HttpStatus;

public enum GlobalErrorCode implements ErrorCode {
  INTERNAL_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred"),
  INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", "Invalid argument provided"),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Requested resource not found"),
  RESOURCE_ALREADY_EXISTS(
      HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS", "Resource already exists"),
  DATABASE_ERROR(HttpStatus.CONFLICT, "DATABASE_ERROR", "Database constraints violated"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required"),
  FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"),
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Order not found"),
  INSUFFICIENT_STOCK(
      HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "Insufficient stock for product");

  private final HttpStatus status;
  private final String code;
  private final String defaultMessage;

  GlobalErrorCode(HttpStatus status, String code, String defaultMessage) {
    this.status = status;
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

  @Override
  public HttpStatus getStatus() {
    return status;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getDefaultMessage() {
    return defaultMessage;
  }
}
