package com.example.identity_service.exception;

import com.example.identity_service.dto.request.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.security.access.AccessDeniedException;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final static String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<String>> handlingRuntimeException(RuntimeException exception){
        ApiResponse<String> apiResponse = new ApiResponse<>();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<String>> handlingRuntimeException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<String> apiResponse = new ApiResponse<>();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<String>> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiResponse<String> apiResponse = new ApiResponse<>();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<String>> handlingValidation(MethodArgumentNotValidException exception){
        ApiResponse<String> apiResponse = new ApiResponse<>();

        String message = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;
        try{
            errorCode = ErrorCode.valueOf(message);
            var constraintViolation = exception.getBindingResult()
                    .getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

        }
        catch(IllegalArgumentException e){

        }
        apiResponse.setMessage(Objects.nonNull(attributes) ?
                mapAttribute(errorCode.getMessage(), attributes)
                : errorCode.getMessage());
        apiResponse.setCode(errorCode.getCode());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message , Map<String, Object> attributes){
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE)) ;
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }

}
