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
class InventorySummaryTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void summarizesStock() throws Exception {
        mvc.perform(get("/api/products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCount").value(3))
                .andExpect(jsonPath("$.totalUnits").value(71))
                .andExpect(jsonPath("$.lowStockCount").value(0));
        mvc.perform(get("/api/products/summary").param("threshold", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStockCount").value(1));
    }

    @Test
    void handlesEmptyStock() throws Exception {
        jdbc.update("DELETE FROM products");
        mvc.perform(get("/api/products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCount").value(0))
                .andExpect(jsonPath("$.totalUnits").value(0))
                .andExpect(jsonPath("$.lowStockCount").value(0));
    }

    @Test
    void handlesLargeTotals() throws Exception {
        jdbc.update("UPDATE products SET quantity = ?", Integer.MAX_VALUE);
        mvc.perform(get("/api/products/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUnits").value(6442450941L));
    }

    @Test
    void rejectsBadThreshold() throws Exception {
        for (String value : new String[]{"-1", "abc"}) {
            mvc.perform(get("/api/products/summary").param("threshold", value))
                    .andExpect(status().isBadRequest());
        }
    }
}
