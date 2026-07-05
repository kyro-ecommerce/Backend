package com.kyro.exceptions;

import org.springframework.http.HttpStatus;

public class DomainException extends AppException {

  public DomainException(HttpStatus status, String message) {
    super(status, "DOMAIN_ERROR", message);
  }

  public DomainException(HttpStatus status, String errorCode, String message) {
    super(status, errorCode, message);
  }
}
