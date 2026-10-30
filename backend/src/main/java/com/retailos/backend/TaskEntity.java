package com.retailos.backend;

import java.time.Instant;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class TaskEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(nullable = false, columnDefinition = "text")
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "text")
    private TaskStatus status = TaskStatus.TODO;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "due_date")
    private LocalDate dueDate;

    protected TaskEntity() {
    }

    public TaskEntity(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
