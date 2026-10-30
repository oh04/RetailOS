package com.retailos.backend;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUserEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(nullable = false, columnDefinition = "text")
    private String email;
    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "text")
    private UserRole role = UserRole.EMPLOYEE;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected AppUserEntity() {
    }

    public AppUserEntity(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
