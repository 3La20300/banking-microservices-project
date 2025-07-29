package com.banking.accountservice.exception;

import com.banking.accountservice.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountNotFound(AccountNotFoundException e) {
        ErrorResponseDto error = new ErrorResponseDto(404, "Not Found", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoAccountsForUserId.class)
    public ResponseEntity<ErrorResponseDto> handleNoAccountsForUserId(NoAccountsForUserId e) {
        ErrorResponseDto error = new ErrorResponseDto(404, "Not Found", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    @ExceptionHandler(InvalidAccountTypeException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAccountType(InvalidAccountTypeException e) {
        ErrorResponseDto error = new ErrorResponseDto(400, "Bad Request", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidAccountDataException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidAccountData(InvalidAccountDataException e) {
        ErrorResponseDto error = new ErrorResponseDto(400, "Bad Request", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        ErrorResponseDto error = new ErrorResponseDto(500, "Internal Server Error", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}