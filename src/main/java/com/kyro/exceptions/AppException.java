package com.kyro.exceptions;

public class AppException extends RuntimeException {
    private String code;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
