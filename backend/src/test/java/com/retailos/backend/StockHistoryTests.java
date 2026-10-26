package com.retailos.backend;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockHistoryTests extends DatabaseTests {
    @Autowired
    private ProductService service;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void recordsChanges() {
        Instant before = Instant.now().minusSeconds(1);
        service.adjustStock(1L, 5).orElseThrow();
        service.adjustStock(1L, -2).orElseThrow();
        var rows = jdbc.queryForList("SELECT * FROM stock_adjustments ORDER BY id");
        assertEquals(2, rows.size());
        assertEquals(1L, ((Number) rows.getFirst().get("product_id")).longValue());
        assertEquals(5, ((Number) rows.getFirst().get("quantity_change")).intValue());
        assertEquals(29, ((Number) rows.getFirst().get("resulting_quantity")).intValue());
        assertEquals(-2, ((Number) rows.getLast().get("quantity_change")).intValue());
        assertEquals(27, ((Number) rows.getLast().get("resulting_quantity")).intValue());
        Instant timestamp = jdbc.queryForObject(
                "SELECT created_at FROM stock_adjustments ORDER BY id LIMIT 1",
                (rs, index) -> rs.getTimestamp(1).toInstant());
        assertNotNull(timestamp);
        assertFalse(timestamp.isBefore(before));
        assertFalse(timestamp.isAfter(Instant.now().plusSeconds(1)));
    }

    @Test
    void skipsRejectedChanges() {
        assertThrows(IllegalArgumentException.class, () -> service.adjustStock(1L, -25));
        assertEquals(24, service.findProductById(1L).orElseThrow().quantity());
        assertEquals(0, count());
        assertTrue(service.adjustStock(999L, 1).isEmpty());
        assertEquals(0, count());
    }

    @Test
    void keepsDeletedHistory() {
        service.adjustStock(1L, -2).orElseThrow();
        assertTrue(service.deleteProduct(1L));
        assertEquals(1, count());
        assertEquals(1L, jdbc.queryForObject(
                "SELECT product_id FROM stock_adjustments", Long.class));
    }

    @Test
    void rollsBackTogether() {
        // A temporary constraint makes the history INSERT fail after changing stock.
        jdbc.execute("ALTER TABLE stock_adjustments ADD CONSTRAINT test_reject_change CHECK (quantity_change <> 5)");
        try {
            assertThrows(RuntimeException.class, () -> service.adjustStock(1L, 5));
            assertEquals(24, service.findProductById(1L).orElseThrow().quantity());
            assertEquals(0, count());
        } finally {
            jdbc.execute("ALTER TABLE stock_adjustments DROP CONSTRAINT test_reject_change");
        }
    }

    @Test
    void recordsRecount() {
        service.updateProduct(1L, "Whole Milk", 10, null, null).orElseThrow();
        var row = jdbc.queryForMap("SELECT * FROM stock_adjustments");
        assertEquals(1L, ((Number) row.get("product_id")).longValue());
        assertEquals(-14, ((Number) row.get("quantity_change")).intValue());
        assertEquals(10, ((Number) row.get("resulting_quantity")).intValue());
        assertNotNull(row.get("created_at"));
    }

    @Test
    void skipsNameOnlyEdit() {
        service.updateProduct(1L, "Fresh Milk", 24, null, null).orElseThrow();
        assertEquals("Fresh Milk", service.findProductById(1L).orElseThrow().name());
        assertEquals(0, count());
    }

    private int count() {
        return jdbc.queryForObject("SELECT count(*) FROM stock_adjustments", Integer.class);
    }
}
