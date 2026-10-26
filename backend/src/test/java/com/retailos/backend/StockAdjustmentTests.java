package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StockAdjustmentTests extends DatabaseTests {
    @Autowired
    private MockMvc mvc;

    @Test
    void receivesStock() throws Exception {
        adjust(1, "{\"change\":5}", 200);
        quantity(1, 29);
    }

    @Test
    void sellsStock() throws Exception {
        mvc.perform(post("/api/products/1/adjustments")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"change\":-2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Whole Milk"))
                .andExpect(jsonPath("$.quantity").value(22));
        quantity(1, 22);
    }

    @Test
    void allowsZero() throws Exception {
        adjust(1, "{\"change\":0}", 200);
        quantity(1, 24);
        adjust(1, "{\"change\":-24}", 200);
        quantity(1, 0);
    }

    @Test
    void rejectsNegativeStock() throws Exception {
        adjust(1, "{\"change\":-25}", 400);
        quantity(1, 24);
        adjust(1, "{\"change\":-2147483648}", 400);
        quantity(1, 24);
    }

    @Test
    void checksUpperLimit() throws Exception {
        adjust(1, "{\"change\":2147483623}", 200);
        quantity(1, Integer.MAX_VALUE);
        adjust(1, "{\"change\":1}", 400);
        quantity(1, Integer.MAX_VALUE);
    }

    @Test
    void missingProduct() throws Exception {
        adjust(999, "{\"change\":1}", 404);
    }

    @Test
    void rejectsBadRequests() throws Exception {
        for (String body : new String[]{"{}", "{\"change\":null}",
                "{\"change\":\"abc\"}", "{\"change\":2147483648}", "{"}) {
            adjust(1, body, 400);
            quantity(1, 24);
        }
    }

    private void adjust(long id, String body, int expectedStatus) throws Exception {
        mvc.perform(post("/api/products/{id}/adjustments", id)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus));
    }

    private void quantity(long id, int expected) throws Exception {
        mvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(expected));
    }
}
