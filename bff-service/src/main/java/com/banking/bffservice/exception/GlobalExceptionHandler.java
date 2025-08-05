package com.banking.bffservice.exception;

import com.banking.bffservice.dto.response.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountsNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDto> handleAccountsNotFoundException(AccountsNotFoundException ex) {
        logger.error("Accounts not found: {}", ex.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.BAD_REQUEST.value(),
            "BAD REQUEST",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
        "BAD REQUEST",
              fieldErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponseDto> handleWebClientResponseException(WebClientResponseException ex) {
        logger.error("WebClient error: {}", ex.getMessage());

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            ex.getStatusCode().value(),
            ex.getStatusText(),
            ex.getMessage()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex) {
        logger.error("Unhandled exception: ", ex);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL SERVER ERROR",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

//    public static class ErrorResponse {
//        private String error;
//        private int status;
//        private String timestamp;
//        private String message;
//
//        public ErrorResponse(String error, int status, String timestamp, String message) {
//            this.error = error;
//            this.status = status;
//            this.timestamp = timestamp;
//            this.message = message;
//        }
//
//        // Getters and setters
//        public String getError() { return error; }
//        public void setError(String error) { this.error = error; }
//        public int getStatus() { return status; }
//        public void setStatus(int status) { this.status = status; }
//        public String getTimestamp() { return timestamp; }
//        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
//        public String getMessage() { return message; }
//        public void setMessage(String message) { this.message = message; }
//    }

    public static class ValidationErrorResponse extends ErrorResponseDto
    {
        private Map<String, String> fieldErrors;

        public ValidationErrorResponse(int status,String error, Map<String, String> fieldErrors) {
            super(status,error , "Validation failed for request fields");
            this.fieldErrors = fieldErrors;
        }

        // Getters and setters
        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
    }
}
