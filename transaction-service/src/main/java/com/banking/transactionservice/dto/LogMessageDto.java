package com.banking.transactionservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessageDto {
    private String message;
    private String messageType; // "Request" or "Response"
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateTime;
    
    private String serviceName;
    private String endpoint;
    private String method;


    public LogMessageDto(String messageContent, String request, String serviceName, String endpoint, String method) {
        this.message = messageContent;
        this.messageType = request;
        this.dateTime = LocalDateTime.now();
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.method = method;
    }
}