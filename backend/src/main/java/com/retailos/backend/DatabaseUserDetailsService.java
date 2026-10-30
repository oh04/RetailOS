package com.retailos.backend;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AppUserRepository repository;

    public DatabaseUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        String newEmail = email.strip().toLowerCase(Locale.ROOT);
        Optional<AppUserEntity> entity = repository.findByEmail(newEmail);
        if (entity.isEmpty()) {
            throw new UsernameNotFoundException("Account not found");
        }
        AppUserEntity account = entity.get();
        return User.withUsername(account.getEmail())
                .password(account.getPasswordHash())
                .roles(account.getRole().name())
                .build();
    }
}
