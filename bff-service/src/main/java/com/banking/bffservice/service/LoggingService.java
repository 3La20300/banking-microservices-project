// LoggingService.java
package com.banking.bffservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.logging}")
    private String loggingTopic;

    public LoggingService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void logRequest(Object request, String endpoint) {
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            sendLogMessage(requestJson, "Request", endpoint);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing request for logging", e);
        }
    }

    public void logResponse(Object response, String endpoint) {
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            sendLogMessage(responseJson, "Response", endpoint);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing response for logging", e);
        }
    }

    private void sendLogMessage(String message, String messageType, String endpoint) {
        try {
            Map<String, Object> logMessage = new HashMap<>();
            logMessage.put("message", message);
            logMessage.put("messageType", messageType);
            logMessage.put("dateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logMessage.put("service", "bff-service");
            logMessage.put("endpoint", endpoint);

            String logJson = objectMapper.writeValueAsString(logMessage);

            kafkaTemplate.send(loggingTopic, logJson)
                    .addCallback(
                            result -> logger.debug("Log message sent successfully for endpoint: {}", endpoint),
                            failure -> logger.error("Failed to send log message for endpoint: {}", endpoint, failure)
                    );
        } catch (JsonProcessingException e) {
            logger.error("Error creating log message", e);
        }
    }
}