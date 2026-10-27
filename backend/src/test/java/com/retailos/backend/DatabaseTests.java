package com.retailos.backend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

// This database is disposable and separate from the development database.
abstract class DatabaseTests {
    private static final PostgreSQLContainer DATABASE =
            new PostgreSQLContainer("postgres:17.11-alpine");

    static { DATABASE.start(); }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void resetProducts() {
        jdbc.execute("TRUNCATE TABLE tasks, stock_adjustments, products RESTART IDENTITY");
        jdbc.update("INSERT INTO products (name, quantity) VALUES (?, ?)", "Whole Milk", 24);
        jdbc.update("INSERT INTO products (name, quantity) VALUES (?, ?)", "Wheat Bread", 15);
        jdbc.update("INSERT INTO products (name, quantity) VALUES (?, ?)", "Paper Towels", 32);
    }
}
