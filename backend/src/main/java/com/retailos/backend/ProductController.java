package com.retailos.backend;

import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@RestController 
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products")
    public List<Product> getProducts() {
        return productService.getProducts();
    }

    @GetMapping("/api/products/{id}")
    public Product getProductById(@PathVariable long id) {
        Optional<Product> product = productService.findProductById(id);
        if (product.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return product.get();
    }

    @GetMapping("/api/products/low-stock")
    public List<Product> getLowStockProducts(@RequestParam(name = "threshold", defaultValue = "10") int threshold) {
        if(threshold < 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Threshold must be a non-negative integer");
        }
        return productService.findLowStockProducts(threshold);
    }
}

