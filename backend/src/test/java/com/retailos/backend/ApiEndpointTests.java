package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiEndpointTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void healthReturnsRunningMessage() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(content().string("RetailOS is running"));
    }

    @Test
    public void getProductsReturnsListOfProducts() throws Exception {
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
}
