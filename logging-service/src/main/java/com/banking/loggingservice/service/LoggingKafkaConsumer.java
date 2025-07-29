package com.banking.loggingservice.service;

import com.banking.loggingservice.dto.LogMessageDto;
import com.banking.loggingservice.model.LogEntry;
import com.banking.loggingservice.repository.LogEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoggingKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(LoggingKafkaConsumer.class);

    @Autowired
    private LogEntryRepository logEntryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "${banking.kafka.topic.logging}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLogMessage(String message) {
        logger.info("Received log message: {}", message);

        try {
            // Parse the JSON message
            LogMessageDto logMessageDto = objectMapper.readValue(message, LogMessageDto.class);

            // Map to entity
            LogEntry logEntry = new LogEntry();
            logEntry.setMessage(logMessageDto.getMessage());
            logEntry.setMessageType(logMessageDto.getMessageType());
            logEntry.setDateTime(logMessageDto.getDateTime() != null ? logMessageDto.getDateTime() : LocalDateTime.now());

            // Save to database
            logEntryRepository.save(logEntry);

            logger.info("Successfully processed and stored log message with type: {}", logMessageDto.getMessageType());
        } catch (JsonProcessingException e) {
            logger.error("Error parsing log message: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing log message: {}", e.getMessage());
        }
    }
}
