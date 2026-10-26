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
class ProductCsvTests extends DatabaseTests {
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void downloadsProducts() throws Exception {
        jdbc.update("UPDATE products SET expiration_date = DATE '2030-06-15' WHERE id = 1");
        String expected = "id,name,quantity,category,expirationDate\r\n"
                + "\"1\",\"Whole Milk\",\"24\",\"Uncategorized\",\"2030-06-15\"\r\n"
                + "\"2\",\"Wheat Bread\",\"15\",\"Uncategorized\",\"\"\r\n"
                + "\"3\",\"Paper Towels\",\"32\",\"Uncategorized\",\"\"\r\n";
        mvc.perform(get("/api/products/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"products.csv\""))
                .andExpect(content().string(expected));
    }

    @Test
    void escapesCells() throws Exception {
        jdbc.update("UPDATE products SET name = ?, category = ? WHERE id = 1",
                "Milk, \"Fresh\"\nLarge", "Dairy, chilled");
        String expectedRow = "\"1\",\"Milk, \"\"Fresh\"\"\nLarge\",\"24\",\"Dairy, chilled\",\"\"\r\n";
        mvc.perform(get("/api/products/export"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedRow)));
    }

    @Test
    void exportsEmptyInventory() throws Exception {
        jdbc.update("DELETE FROM products");
        mvc.perform(get("/api/products/export"))
                .andExpect(status().isOk())
                .andExpect(content().string("id,name,quantity,category,expirationDate\r\n"));
    }
    @Test
    void filtersExport() throws Exception {
        jdbc.update("UPDATE products SET category = 'Dairy' WHERE id = 1");
        String expected = "id,name,quantity,category,expirationDate\r\n"
                + "\"1\",\"Whole Milk\",\"24\",\"Dairy\",\"\"\r\n";
        mvc.perform(get("/api/products/export").param("category", "  dAiRy  "))
                .andExpect(status().isOk()).andExpect(content().string(expected));
        mvc.perform(get("/api/products/export").param("category", "Produce"))
                .andExpect(status().isOk())
                .andExpect(content().string("id,name,quantity,category,expirationDate\r\n"));
    }

    @Test
    void rejectsBlankCategory() throws Exception {
        for (String category : new String[]{"", "   "}) {
            mvc.perform(get("/api/products/export").param("category", category))
                    .andExpect(status().isBadRequest());
        }
    }

}
