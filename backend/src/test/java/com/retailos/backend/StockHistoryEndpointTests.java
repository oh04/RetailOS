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
class StockHistoryEndpointTests extends DatabaseTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProductService service;

    @Test
    void listsHistory() throws Exception {
        service.adjustStock(1L, 5);
        service.adjustStock(1L, -2);
        service.adjustStock(2L, 3);
        mvc.perform(get("/api/products/1/adjustments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].change").value(-2))
                .andExpect(jsonPath("$[0].resultingQuantity").value(27))
                .andExpect(jsonPath("$[0].createdAt").isString())
                .andExpect(jsonPath("$[1].change").value(5))
                .andExpect(jsonPath("$[1].resultingQuantity").value(29));
    }

    @Test
    void emptyHistory() throws Exception {
        mvc.perform(get("/api/products/1/adjustments"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/products/999/adjustments"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void readsDeletedHistory() throws Exception {
        service.adjustStock(1L, -2);
        service.deleteProduct(1L);
        mvc.perform(get("/api/products/1/adjustments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].change").value(-2));
    }

    @Test
    void rejectsBadId() throws Exception {
        mvc.perform(get("/api/products/abc/adjustments"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void limitsHistory() throws Exception {
        for (int i = 0; i < 22; i++) {
            service.adjustStock(1L, 1);
        }
        mvc.perform(get("/api/products/1/adjustments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20));
        mvc.perform(get("/api/products/1/adjustments").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].resultingQuantity").value(46))
                .andExpect(jsonPath("$[1].resultingQuantity").value(45));
        mvc.perform(get("/api/products/1/adjustments").param("limit", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(22));
    }

    @Test
    void rejectsBadLimit() throws Exception {
        for (String limit : new String[]{"0", "-1", "101", "abc"}) {
            mvc.perform(get("/api/products/1/adjustments").param("limit", limit))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void pagesHistory() throws Exception {
        for (int i = 0; i < 5; i++) {
            service.adjustStock(1L, 1);
        }
        mvc.perform(get("/api/products/1/adjustments").param("limit", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].resultingQuantity").value(29))
                .andExpect(jsonPath("$[1].resultingQuantity").value(28));
        mvc.perform(get("/api/products/1/adjustments").param("limit", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].resultingQuantity").value(27))
                .andExpect(jsonPath("$[1].resultingQuantity").value(26));
        mvc.perform(get("/api/products/1/adjustments").param("limit", "2").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].resultingQuantity").value(25));
        mvc.perform(get("/api/products/1/adjustments").param("limit", "2").param("page", "3"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void rejectsBadPage() throws Exception {
        for (String page : new String[]{"-1", "abc"}) {
            mvc.perform(get("/api/products/1/adjustments").param("page", page))
                    .andExpect(status().isBadRequest());
        }
    }

}
