package com.banking.loggingservice.repository;

import com.banking.loggingservice.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntry, UUID> {
}
