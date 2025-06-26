package com.example.identity_service.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized error"),
    INVALID_KEY(1000,"Invalid key"),
    USER_EXISTED(1001,"User existed"),
    USERNAME_INVALID(1002,"Username must be at least 3 characters"),
    INVALID_PASSWORD(1003, "Message must be at least 8 characters"),
    USER_NOT_EXISTED(1004,"User arent existed")
    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
