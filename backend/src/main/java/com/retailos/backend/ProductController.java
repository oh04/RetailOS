package com.retailos.backend;

import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController 
public class ProductController {

    @GetMapping("/api/products")
    public List<Product> getProducts() {
        return List.of(
            new Product(1L, "Whole Milk", 24),
            new Product(2L, "Wheat Bread", 15),
            new Product(3L, "Paper Towels", 32)
        );
    }
}
