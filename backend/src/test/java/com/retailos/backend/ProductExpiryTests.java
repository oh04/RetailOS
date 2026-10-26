package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductExpiryTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void readsExpiry() throws Exception {
        jdbc.update("UPDATE products SET expiration_date = DATE '2030-06-15' WHERE id = 1");
        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value("2030-06-15"));
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].expirationDate").value("2030-06-15"));
    }

    @Test
    void allowsNoExpiry() throws Exception {
        mvc.perform(get("/api/products/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value(nullValue()));
    }
    @Test
    void createsExpiry() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Yogurt\",\"quantity\":5,\"expirationDate\":\"2030-06-15\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expirationDate").value("2030-06-15"));
        mvc.perform(get("/api/products/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value("2030-06-15"));
    }

    @Test
    void changesAndClearsExpiry() throws Exception {
        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Whole Milk\",\"quantity\":24,\"expirationDate\":\"2030-07-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value("2030-07-01"));
        mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Whole Milk\",\"quantity\":24,\"expirationDate\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value(nullValue()));
        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expirationDate").value(nullValue()));
        mvc.perform(get("/api/products/1/adjustments"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsBadDates() throws Exception {
        for (String date : new String[]{"tomorrow", "2030-13-01", "2030-02-30"}) {
            String body = "{\"name\":\"Milk\",\"quantity\":24,\"expirationDate\":\"" + date + "\"}";
            mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/api/products/1").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

}
