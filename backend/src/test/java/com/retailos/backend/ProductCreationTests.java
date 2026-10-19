package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductCreationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  Orange Juice  ","quantity":8}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/4"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Orange Juice"))
                .andExpect(jsonPath("$.quantity").value(8));

        mockMvc.perform(get("/api/products/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Orange Juice"));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void assignsIds() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Apples","quantity":0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.quantity").value(0));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bananas","quantity":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void rejectsNames() throws Exception {
        String[] requests = {
                "{\"quantity\":8}",
                "{\"name\":null,\"quantity\":8}",
                "{\"name\":\"\",\"quantity\":8}",
                "{\"name\":\"   \",\"quantity\":8}"
        };

        for (String request : requests) {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        assertInventoryUnchanged();
    }

    @Test
    void rejectsQuantities() throws Exception {
        String[] requests = {
                "{\"name\":\"Juice\",\"quantity\":-1}",
                "{\"name\":\"Juice\"}",
                "{\"name\":\"Juice\",\"quantity\":null}",
                "{\"name\":\"Juice\",\"quantity\":\"abc\"}"
        };

        for (String request : requests) {
            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isBadRequest());
        }

        assertInventoryUnchanged();
    }

    @Test
    void rejectsBadJson() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());

        assertInventoryUnchanged();
    }

    private void assertInventoryUnchanged() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
