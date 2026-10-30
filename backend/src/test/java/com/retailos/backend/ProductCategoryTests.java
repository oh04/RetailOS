package com.retailos.backend;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;
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
@WithMockUser(roles = "MANAGER")
class ProductCategoryTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void readsCategory() throws Exception {
        jdbc.update("UPDATE products SET category = ? WHERE id = ?", "Dairy", 1L);
        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Dairy"));
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Dairy"))
                .andExpect(jsonPath("$[1].category").value("Uncategorized"));
    }

    @Test
    void defaultsCategory() throws Exception {
        mvc.perform(post("/api/products").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Apples\",\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Uncategorized"));
        mvc.perform(get("/api/products/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Uncategorized"));
    }
    @Test
    void createsCategory() throws Exception {
        mvc.perform(post("/api/products").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Yogurt\",\"quantity\":5,\"category\":\"  Dairy  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Dairy"));
        mvc.perform(get("/api/products/4"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("Dairy"));
    }

    @Test
    void updatesCategory() throws Exception {
        mvc.perform(put("/api/products/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Whole Milk\",\"quantity\":24,\"category\":\"  Dairy  \"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("Dairy"));
        mvc.perform(put("/api/products/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fresh Milk\",\"quantity\":24}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("Dairy"));
        mvc.perform(put("/api/products/1").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fresh Milk\",\"quantity\":24,\"category\":null}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("Dairy"));
        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.category").value("Dairy"));
        mvc.perform(get("/api/products/1/adjustments"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsBlankCategory() throws Exception {
        for (String category : new String[]{"", "   "}) {
            String body = "{\"name\":\"Milk\",\"quantity\":24,\"category\":\"" + category + "\"}";
            mvc.perform(post("/api/products").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/api/products/1").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        mvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Whole Milk"))
                .andExpect(jsonPath("$.category").value("Uncategorized"));
    }

    @Test
    void filtersCategory() throws Exception {
        jdbc.update("UPDATE products SET category = 'Dairy' WHERE id IN (1, 3)");
        jdbc.update("UPDATE products SET category = 'Dairy Alternatives' WHERE id = 2");
        mvc.perform(get("/api/products").param("category", "  dAiRy  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(3));
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void noCategoryMatches() throws Exception {
        mvc.perform(get("/api/products").param("category", "Produce"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsBlankFilter() throws Exception {
        for (String category : new String[]{"", "   "}) {
            mvc.perform(get("/api/products").param("category", category))
                    .andExpect(status().isBadRequest());
        }
    }

}
