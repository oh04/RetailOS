package com.retailos.backend;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(long id, String title, TaskStatus status, Instant createdAt, LocalDate dueDate) {
    
}
