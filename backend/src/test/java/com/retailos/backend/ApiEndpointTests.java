package com.retailos.backend;

import org.springframework.security.test.context.support.WithMockUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "MANAGER")
public class ApiEndpointTests extends DatabaseTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void healthWorks() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(content().string("RetailOS is running"));
    }

    @Test
    public void listsProducts() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Whole Milk"))
                .andExpect(jsonPath("$[0].quantity").value(24))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Wheat Bread"))
                .andExpect(jsonPath("$[1].quantity").value(15))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("Paper Towels"))
                .andExpect(jsonPath("$[2].quantity").value(32));
    }

    @Test
    public void findsProduct() throws Exception {
        mockMvc.perform(get("/api/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Wheat Bread"))
                .andExpect(jsonPath("$.quantity").value(15));
    }

    @Test
    public void missingProduct() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void badProductId() throws Exception {
        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void filtersLowStock() throws Exception {
        mockMvc.perform(get("/api/products/low-stock?threshold=25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Whole Milk"))
                .andExpect(jsonPath("$[0].quantity").value(24))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Wheat Bread"))
                .andExpect(jsonPath("$[1].quantity").value(15));
    }


    @Test
    public void includesThreshold() throws Exception {
        mockMvc.perform(get("/api/products/low-stock?threshold=15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Wheat Bread"))
                .andExpect(jsonPath("$[0].quantity").value(15));
    }

    @Test
    public void defaultsThreshold() throws Exception {
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void rejectsNegativeThreshold() throws Exception {
        mockMvc.perform(get("/api/products/low-stock?threshold=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Threshold must be a non-negative integer"));
    }

    @Test
    public void rejectsBadThreshold() throws Exception {
        mockMvc.perform(get("/api/products/low-stock").param("threshold", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void keepsInventory() throws Exception {
        mockMvc.perform(get("/api/products/low-stock").param("threshold", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].quantity").value(24))
                .andExpect(jsonPath("$[1].quantity").value(15))
                .andExpect(jsonPath("$[2].quantity").value(32));
    }

    @Test
    public void searchesNames() throws Exception {
        mockMvc.perform(get("/api/products/search").param("q", "mIl"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Whole Milk"));
    }

    @Test
    public void trimsSearch() throws Exception {
        mockMvc.perform(get("/api/products/search").param("q", "  BREAD  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].name").value("Wheat Bread"));
    }

    @Test
    public void noSearchMatches() throws Exception {
        mockMvc.perform(get("/api/products/search").param("q", "soap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void rejectsBlankSearch() throws Exception {
        mockMvc.perform(get("/api/products/search").param("q", ""))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/products/search").param("q", " \t "))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void requiresSearchQuery() throws Exception {
        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isBadRequest());
    }
}
