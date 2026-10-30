package com.retailos.backend;

public record UserResponse(long id, String email, UserRole role) {
}
