package com.example.identity_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1000,"Invalid key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001,"User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1002,"Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003, "Message must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1004,"User arent existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1005, "User is not authenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1006, "You do not have permisson", HttpStatus.FORBIDDEN),
    INVALID_DOB(1007, "Your age must be at least {min}", HttpStatus.BAD_REQUEST)
    ;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    private HttpStatusCode httpStatusCode;
    private int code;
    private String message;

}
