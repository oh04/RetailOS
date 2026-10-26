package com.retailos.backend;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockConcurrencyTests extends DatabaseTests {
    @Autowired
    private ProductService service;

    @Test
    void keepsAllChanges() throws Exception {
        runTogether(12, () -> {
            service.adjustStock(1L, 1).orElseThrow();
            return true;
        });
        assertEquals(36, service.findProductById(1L).orElseThrow().quantity());
    }

    @Test
    void preventsOverselling() throws Exception {
        var outcomes = runTogether(2, () -> {
            try {
                service.adjustStock(1L, -20).orElseThrow();
                return true;
            } catch (IllegalArgumentException expected) {
                return false;
            }
        });
        assertEquals(1L, outcomes.stream().filter(Boolean::booleanValue).count());
        assertEquals(4, service.findProductById(1L).orElseThrow().quantity());
    }

    private java.util.List<Boolean> runTogether(int count, Callable<Boolean> action) throws Exception {
        var ready = new CountDownLatch(count);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(count)) {
            var futures = new ArrayList<java.util.concurrent.Future<Boolean>>();
            try {
                for (int i = 0; i < count; i++) {
                    futures.add(executor.submit(() -> {
                        ready.countDown();
                        if (!start.await(10, TimeUnit.SECONDS)) {
                            throw new AssertionError("Workers did not start together");
                        }
                        return action.call();
                    }));
                }
                assertTrue(ready.await(10, TimeUnit.SECONDS));
                start.countDown();
                var results = new ArrayList<Boolean>();
                for (var future : futures) {
                    results.add(future.get(20, TimeUnit.SECONDS));
                }
                return results;
            } finally {
                start.countDown();
                executor.shutdownNow();
            }
        }
    }
}
