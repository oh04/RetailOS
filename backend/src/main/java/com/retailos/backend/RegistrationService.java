package com.retailos.backend;

import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class RegistrationService {

    private final AppUserRepository repository;
    private final PasswordEncoder encoder;

    public RegistrationService(AppUserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(String email, String password) {
        String newEmail = email.strip().toLowerCase(Locale.ROOT);
        String newPasswordHash = encoder.encode(password);
        AppUserEntity entity = new AppUserEntity(newEmail, newPasswordHash);
        AppUserEntity saved = repository.saveAndFlush(entity);
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getRole());
    }
}
