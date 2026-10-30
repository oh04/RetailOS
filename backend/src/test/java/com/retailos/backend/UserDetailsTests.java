package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDetailsTests extends DatabaseTests {
    @Autowired private UserDetailsService service;
    @Autowired private AppUserRepository repository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void loadsEmployee() {
        String hash = encoder.encode("FictionalPass123!");
        repository.saveAndFlush(new AppUserEntity("employee@example.test", hash));
        var user = service.loadUserByUsername("  EMPLOYEE@example.test  ");
        assertEquals("employee@example.test", user.getUsername());
        assertEquals(hash, user.getPassword());
        assertTrue(encoder.matches("FictionalPass123!", user.getPassword()));
        assertEquals(java.util.Set.of("ROLE_EMPLOYEE"), user.getAuthorities().stream()
                .map(authority -> authority.getAuthority()).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void loadsManager() {
        repository.saveAndFlush(new AppUserEntity("manager@example.test", encoder.encode("FictionalPass123!")));
        // Fixture promotion only; public registration must never accept a manager role.
        jdbc.update("UPDATE app_users SET role = 'MANAGER' WHERE email = 'manager@example.test'");
        var user = service.loadUserByUsername("manager@example.test");
        assertEquals(java.util.Set.of("ROLE_MANAGER"), user.getAuthorities().stream()
                .map(authority -> authority.getAuthority()).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void missingAccount() {
        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.test"));
    }
}
