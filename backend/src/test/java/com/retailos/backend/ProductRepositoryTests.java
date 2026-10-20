package com.retailos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductRepositoryTests extends DatabaseTests {
    @Autowired
    private ProductRepository repository;

    @Test
    void savesProduct() {
        ProductEntity saved = repository.saveAndFlush(new ProductEntity("Apples", 0));
        assertEquals(4L, saved.getId());
        ProductEntity loaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Apples", loaded.getName());
        assertEquals(0, loaded.getQuantity());
    }

    @Test
    void queriesProducts() {
        var lowStock = repository.findByQuantityLessThanEqualOrderByIdAsc(15);
        assertEquals(1, lowStock.size());
        assertEquals("Wheat Bread", lowStock.getFirst().getName());
        var matches = repository.findByNameContainingIgnoreCaseOrderByIdAsc("mIL");
        assertEquals(1, matches.size());
        assertEquals("Whole Milk", matches.getFirst().getName());
        assertEquals(3, repository.findAllByOrderByIdAsc().size());
    }
}
