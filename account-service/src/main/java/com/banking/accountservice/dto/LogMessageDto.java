package com.banking.accountservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class LogMessageDto {
    private String message;
    private String messageType;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime dateTime;

    private String serviceName;
    private String endpoint;
    private String method;

    //Default_Constructor
    public LogMessageDto() {
        this.dateTime=LocalDateTime.now();
    }
    public LogMessageDto(String message, String messageType, String serviceName, String endpoint, String method) {
        this.message = message;
        this.messageType = messageType;
        this.serviceName = serviceName;
        this.dateTime=LocalDateTime.now();
        this.endpoint = endpoint;
        this.method = method;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
