package com.retailos.backend;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService{

    private final List<Product> products = List.of(
        new Product(1L, "Whole Milk", 24),
        new Product(2L, "Wheat Bread", 15),
        new Product(3L, "Paper Towels", 32)
    );

    public List<Product> getProducts() {
        return products;
    }

    public Optional<Product> findProductById(long id) {

        for (Product product : products) {
            if (product.id() == id) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }
    
    public List<Product> findLowStockProducts(int threshold){
        ArrayList<Product> lowStockProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.quantity() <= threshold) {
                lowStockProducts.add(product);
            }
        }
        return lowStockProducts;
    }
}
