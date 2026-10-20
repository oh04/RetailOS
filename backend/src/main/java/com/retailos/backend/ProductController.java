package com.retailos.backend;

import java.net.URI;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/api/products/search")
    public List<Product> getProduct(@RequestParam("q") String query){
        if(query.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query parameter can't be empty");
        }
        return productService.searchProducts(query);
    }

    @PostMapping("/api/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.addProduct(request.name(), request.quantity());
        return ResponseEntity.created(URI.create("/api/products/" + product.id())).body(product);
    }

    @PutMapping("/api/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable long id, @Valid @RequestBody UpdateProductRequest request) {
        Optional<Product> updatedProduct = productService.updateProduct(id, request.name(), request.quantity());
        if (updatedProduct.isPresent()) {
            return ResponseEntity.ok(updatedProduct.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

    @DeleteMapping ("/api/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) {
        boolean deleted = productService.deleteProduct(id);
        if(!deleted){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return ResponseEntity.noContent().build();
    }
}
