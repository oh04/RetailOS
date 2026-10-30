package com.retailos.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SessionSecurityTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private AppUserRepository repository;
    @Autowired private PasswordEncoder encoder;

    @Test
    void publicHealth() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void requiresLogin() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void logsInAndOut() throws Exception {
        repository.saveAndFlush(new AppUserEntity("employee@example.test", encoder.encode("FictionalPass123!")));
        var csrfResult = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.token").isString())
                .andReturn();
        String body = csrfResult.getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.token");
        String header = JsonPath.read(body, "$.headerName");
        MockHttpSession initial = (MockHttpSession) csrfResult.getRequest().getSession(false);
        assertNotNull(initial);
        var login = mvc.perform(post("/api/auth/login").session(initial)
                        .header(header, token)
                        .param("username", "employee@example.test")
                        .param("password", "FictionalPass123!"))
                .andExpect(status().isNoContent()).andReturn();
        MockHttpSession authenticated = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(authenticated);
        mvc.perform(get("/api/products").session(authenticated))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").session(authenticated).with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(authenticated.isInvalid());
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        repository.saveAndFlush(new AppUserEntity("employee@example.test", encoder.encode("FictionalPass123!")));
        mvc.perform(post("/api/auth/login").with(csrf())
                        .param("username", "employee@example.test").param("password", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requiresCsrf() throws Exception {
        String body = "{\"name\":\"Apples\",\"quantity\":3}";
        mvc.perform(post("/api/products").with(user("manager").roles("MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/products").with(user("manager").roles("MANAGER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void currentAccount() throws Exception {
        var account = repository.saveAndFlush(new AppUserEntity(
                "employee@example.test", encoder.encode("FictionalPass123!")));
        mvc.perform(get("/api/auth/me").with(user("employee@example.test").roles("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(account.getId()))
                .andExpect(jsonPath("$.email").value("employee@example.test"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void rejectsDeletedAccount() throws Exception {
        mvc.perform(get("/api/auth/me").with(user("missing@example.test").roles("EMPLOYEE")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicRegistration() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@example.test\",\"password\":\"FictionalPass123!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }
}
