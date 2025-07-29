package com.banking.loggingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "log_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
