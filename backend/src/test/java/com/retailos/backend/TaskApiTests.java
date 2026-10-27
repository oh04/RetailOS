package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void createsTask() throws Exception {
        create();
        mvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Restock dairy"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void listsTasks() throws Exception {
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        create();
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void changesStatus() throws Exception {
        create();
        for (String value : new String[]{"IN_PROGRESS", "DONE", "TODO"}) {
            mvc.perform(patch("/api/tasks/1/status").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"" + value + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(value))
                    .andExpect(jsonPath("$.title").value("Restock dairy"));
            mvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(value));
        }
    }

    @Test
    void missingTask() throws Exception {
        mvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/tasks/999/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsBadTitle() throws Exception {
        for (String body : new String[]{"{}", "{\"title\":null}", "{\"title\":\"\"}", "{\"title\":\"   \"}"}) {
            mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsBadStatus() throws Exception {
        create();
        for (String body : new String[]{"{}", "{\"status\":null}", "{\"status\":\"UNKNOWN\"}"}) {
            mvc.perform(patch("/api/tasks/1/status").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void filtersStatus() throws Exception {
        jdbc.update("INSERT INTO tasks (title, status) VALUES ('Restock dairy', 'TODO'), ('Clean shelves', 'DONE'), ('Check labels', 'TODO')");
        mvc.perform(get("/api/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(3));
        mvc.perform(get("/api/tasks").param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
        mvc.perform(get("/api/tasks").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void rejectsUnknownFilter() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsDueDate() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Check labels\",\"dueDate\":\"2030-06-15\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dueDate").value("2030-06-15"));
        mvc.perform(patch("/api/tasks/1/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2030-06-15"));
        mvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dueDate").value("2030-06-15"));
    }

    @Test
    void allowsNoDueDate() throws Exception {
        create();
        mvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void rejectsBadDueDate() throws Exception {
        for (String date : new String[]{"tomorrow", "2030-13-01", "2030-02-30"}) {
            mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Check labels\",\"dueDate\":\"" + date + "\"}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    private void create() throws Exception {
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  Restock dairy  \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Restock dairy"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.createdAt").isString());
    }
}
