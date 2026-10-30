package com.retailos.backend;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AppUserRepository repository;

    public AccountService(AppUserRepository repository) {
        this.repository = repository;
    }

    public Optional<UserResponse> findByEmail(String email) {
        Optional<AppUserEntity> result = repository.findByEmail(email);
        if (result.isEmpty()) {
            return Optional.empty();
        }
        AppUserEntity account = result.get();
        UserResponse response = new UserResponse(account.getId(), account.getEmail(), account.getRole());
        return Optional.of(response);
    }
}
