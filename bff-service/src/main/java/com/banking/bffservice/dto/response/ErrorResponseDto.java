package com.banking.bffservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;
    private String error;
    private String message;

    // Default constructor

}