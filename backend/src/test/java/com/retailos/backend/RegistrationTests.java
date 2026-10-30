package com.retailos.backend;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "MANAGER")
class RegistrationTests extends DatabaseTests {
    private static final String PASSWORD = "FictionalPass123!";
    @Autowired private MockMvc mvc;
    @Autowired private AppUserRepository repository;
    @Autowired private PasswordEncoder encoder;

    @Test
    void registersEmployee() throws Exception {
        register("Employee@Example.test", PASSWORD, 201);
        var user = repository.findByEmail("employee@example.test").orElseThrow();
        assertEquals(UserRole.EMPLOYEE, user.getRole());
        assertNotEquals(PASSWORD, user.getPasswordHash());
        assertTrue(encoder.matches(PASSWORD, user.getPasswordHash()));
    }

    @Test
    void rejectsDuplicate() throws Exception {
        register("employee@example.test", PASSWORD, 201);
        register("EMPLOYEE@example.test", "AnotherPassword123!", 409);
        assertEquals(1L, repository.count());
        assertTrue(encoder.matches(PASSWORD,
                repository.findByEmail("employee@example.test").orElseThrow().getPasswordHash()));
    }

    @Test
    void validatesInput() throws Exception {
        for (String body : new String[]{"{}", "{\"email\":\"bad-email\",\"password\":\"FictionalPass123!\"}",
                "{\"email\":\"employee@example.test\",\"password\":\"short\"}",
                "{\"email\":\"employee@example.test\",\"password\":null}",
                "{\"email\":null,\"password\":\"FictionalPass123!\"}"}) {
            mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        assertEquals(0L, repository.count());
    }

    @Test
    void rejectsLongUtf8Password() throws Exception {
        register("employee@example.test", "é".repeat(40), 400);
        assertEquals(0L, repository.count());
    }

    @Test
    void saltsPasswords() throws Exception {
        register("first@example.test", PASSWORD, 201);
        register("second@example.test", PASSWORD, 201);
        var first = repository.findByEmail("first@example.test").orElseThrow();
        var second = repository.findByEmail("second@example.test").orElseThrow();
        assertNotEquals(first.getPasswordHash(), second.getPasswordHash());
        assertTrue(encoder.matches(PASSWORD, second.getPasswordHash()));
    }

    private void register(String email, String password, int expectedStatus) throws Exception {
        var result = mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"role\":\"MANAGER\"}"))
                .andExpect(status().is(expectedStatus));
        if (expectedStatus == 201) {
            result.andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.email").value(email.toLowerCase(java.util.Locale.ROOT)))
                    .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.passwordHash").doesNotExist());
        }
    }
}
