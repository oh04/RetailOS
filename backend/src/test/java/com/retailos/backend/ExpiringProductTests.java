package com.retailos.backend;

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
class ExpiringProductTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void findsExpiringProducts() throws Exception {
        jdbc.update("UPDATE products SET expiration_date = DATE '2030-06-15' WHERE id = 1");
        jdbc.update("UPDATE products SET expiration_date = DATE '2030-06-10' WHERE id = 2");
        mvc.perform(get("/api/products/expiring").param("through", "2030-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].expirationDate").value("2030-06-15"));
        mvc.perform(get("/api/products/expiring").param("through", "2030-06-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
        jdbc.update("UPDATE products SET expiration_date = DATE '2030-06-15'");
        mvc.perform(get("/api/products/expiring").param("through", "2030-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[2].id").value(3));
    }

    @Test
    void noExpiringProducts() throws Exception {
        mvc.perform(get("/api/products/expiring").param("through", "2030-06-15"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        jdbc.update("UPDATE products SET expiration_date = DATE '2031-01-01'");
        mvc.perform(get("/api/products/expiring").param("through", "2030-06-15"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsBadCutoff() throws Exception {
        mvc.perform(get("/api/products/expiring"))
                .andExpect(status().isBadRequest());
        for (String date : new String[]{"", "tomorrow", "2030-13-01", "2030-02-30"}) {
            mvc.perform(get("/api/products/expiring").param("through", date))
                    .andExpect(status().isBadRequest());
        }
    }
}
