package com.retailos.backend;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProductService{

    private final List<Product> products = new ArrayList<>(List.of(
        new Product(1L, "Whole Milk", 24),
        new Product(2L, "Wheat Bread", 15),
        new Product(3L, "Paper Towels", 32)
    ));
    private long nextId = 4;

    public synchronized List<Product> getProducts() {
        return List.copyOf(products);
    }

    public synchronized Optional<Product> findProductById(long id) {

        for (Product product : products) {
            if (product.id() == id) {
                return Optional.of(product);
            }
        }
        return Optional.empty();
    }
    
    public synchronized List<Product> findLowStockProducts(int threshold){
        ArrayList<Product> lowStockProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.quantity() <= threshold) {
                lowStockProducts.add(product);
            }
        }
        return lowStockProducts;
    }

    public synchronized List<Product> searchProducts(String query){
        ArrayList<Product> matchingProducts = new ArrayList<>();
        String normalizedQuery = query.strip().toLowerCase(Locale.ROOT);
        for (Product product : products) {
            if (product.name().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                matchingProducts.add(product);
            }
        }
        return matchingProducts;
    }

    public synchronized Product addProduct(String name, int quantity){
        Product newProduct = new Product(nextId, name.strip(), quantity);
        nextId++;
        products.add(newProduct);
        return newProduct;
    }

    public synchronized Optional<Product> updateProduct(long id, String name, int quantity){
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.id() == id) {
                Product updatedProduct = new Product(id, name.strip(), quantity);
                products.set(i, updatedProduct);
                return Optional.of(updatedProduct);
            }
        }
        return Optional.empty();
    }

    public synchronized boolean deleteProduct(long id){
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.id() == id) {
                products.remove(i);
                return true;
            }
        }
        return false;
    }
}