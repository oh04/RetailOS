package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AppUserRepositoryTests extends DatabaseTests {
    @Autowired private AppUserRepository repository;

    @Test
    void savesEmployee() {
        // Storage fixture only: password hashing is tested when registration is implemented.
        AppUserEntity saved = repository.saveAndFlush(
                new AppUserEntity("employee@example.test", "test-only-hash-placeholder"));
        var loaded = repository.findByEmail("employee@example.test").orElseThrow();
        assertEquals(saved.getId(), loaded.getId());
        assertEquals(UserRole.EMPLOYEE, loaded.getRole());
        assertEquals("test-only-hash-placeholder", loaded.getPasswordHash());
        assertNotNull(loaded.getCreatedAt());
        assertTrue(repository.findByEmail("missing@example.test").isEmpty());
    }

    @Test
    void rejectsDuplicateEmail() {
        repository.saveAndFlush(new AppUserEntity("employee@example.test", "first-test-hash"));
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(
                new AppUserEntity("employee@example.test", "second-test-hash")));
    }
}
