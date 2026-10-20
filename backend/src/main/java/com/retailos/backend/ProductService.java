package com.retailos.backend;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService{

    private final ProductRepository repository;

    private Product toProduct(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getQuantity());
    }

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getProducts() {
        return repository.findAllByOrderByIdAsc().stream().map(this::toProduct).toList();
    }

    public Optional<Product> findProductById(long id) {
        return repository.findById(id).map(this::toProduct);
    }
    
    public List<Product> findLowStockProducts(int threshold){
        return repository.findByQuantityLessThanEqualOrderByIdAsc(threshold).stream().map(this::toProduct).toList();
    }

    public List<Product> searchProducts(String query){
        return repository.findByNameContainingIgnoreCaseOrderByIdAsc(query.strip()).stream().map(this::toProduct).toList();
    }

    public Product addProduct(String name, int quantity){
        ProductEntity entity = new ProductEntity(name.strip(), quantity);
        ProductEntity savedEntity = repository.save(entity);
        return toProduct(savedEntity);
    }

    @Transactional
    public Optional<Product> updateProduct(long id, String name, int quantity){
        Optional<ProductEntity> optionalEntity = repository.findById(id);
        if(optionalEntity.isEmpty()){
            return Optional.empty();
        }
        ProductEntity entity = optionalEntity.get();
        entity.setName(name.strip());
        entity.setQuantity(quantity);
        ProductEntity updatedEntity = repository.save(entity);
        return Optional.of(toProduct(updatedEntity));
    }

    @Transactional
    public boolean deleteProduct(long id){
        if(!repository.existsById(id)){
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}