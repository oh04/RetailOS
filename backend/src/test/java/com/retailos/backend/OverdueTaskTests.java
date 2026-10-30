package com.retailos.backend;

import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "MANAGER")
class OverdueTaskTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void findsOverdueTasks() throws Exception {
        jdbc.update("INSERT INTO tasks (title,status,due_date) VALUES "
                + "('Check labels','TODO',DATE '2030-06-14'),"
                + "('Clean shelves','IN_PROGRESS',DATE '2030-06-10'),"
                + "('Completed','DONE',DATE '2030-06-01'),"
                + "('Due today','TODO',DATE '2030-06-15'),"
                + "('No deadline','TODO',NULL),"
                + "('Future','TODO',DATE '2030-06-16'),"
                + "('Second early task','TODO',DATE '2030-06-10')");
        mvc.perform(get("/api/tasks/overdue").param("before", "2030-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(7))
                .andExpect(jsonPath("$[2].id").value(1))
                .andExpect(jsonPath("$[0].dueDate").value("2030-06-10"));
    }

    @Test
    void noOverdueTasks() throws Exception {
        mvc.perform(get("/api/tasks/overdue").param("before", "2030-06-15"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsBadCutoff() throws Exception {
        mvc.perform(get("/api/tasks/overdue"))
                .andExpect(status().isBadRequest());
        for (String cutoff : new String[]{"", "tomorrow", "2030-13-01", "2030-02-30"}) {
            mvc.perform(get("/api/tasks/overdue").param("before", cutoff))
                    .andExpect(status().isBadRequest());
        }
    }
}
