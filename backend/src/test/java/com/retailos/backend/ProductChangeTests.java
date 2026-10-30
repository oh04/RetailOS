package com.retailos.backend;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(roles = "MANAGER")
class ProductChangeTests extends DatabaseTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void updatesProduct() throws Exception {
        mockMvc.perform(put("/api/products/2")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  White Bread  ","quantity":4}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("White Bread"))
                .andExpect(jsonPath("$.quantity").value(4));

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("White Bread"))
                .andExpect(jsonPath("$.quantity").value(4));

        mockMvc.perform(get("/api/products/low-stock").param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void rejectsBadUpdate() throws Exception {
        String[] requests = {
                "{\"name\":\"   \",\"quantity\":4}",
                "{\"name\":\"Bread\",\"quantity\":-1}",
                "{\"name\":\"Bread\"}",
                "{\"name\":\"Bread\",\"quantity\":null}"
        };

        for (String request : requests) {
            mockMvc.perform(put("/api/products/2")
                            .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Wheat Bread"))
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    void missingUpdate() throws Exception {
        mockMvc.perform(put("/api/products/999")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bread","quantity":4}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void deletesProduct() throws Exception {
        mockMvc.perform(delete("/api/products/2").with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/products/search").param("q", "bread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void missingDelete() throws Exception {
        mockMvc.perform(delete("/api/products/999").with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void keepsIdsAfterDelete() throws Exception {
        mockMvc.perform(delete("/api/products/2").with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/products")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Apples","quantity":0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.quantity").value(0));
    }
}
