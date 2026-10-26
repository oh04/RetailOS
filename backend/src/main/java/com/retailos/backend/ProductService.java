package com.retailos.backend;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService{

    private final ProductRepository repository;
    private final StockAdjustmentRepository adjustmentRepository;

    private Product toProduct(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getQuantity(), entity.getCategory(), entity.getExpirationDate());
    }

    public ProductService(ProductRepository repository, StockAdjustmentRepository adjustmentRepository) {
        this.repository = repository;
        this.adjustmentRepository = adjustmentRepository;
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

    public Product addProduct(String name, int quantity, String category, LocalDate expirationDate){
        ProductEntity entity = new ProductEntity(name.strip(), quantity);
        if(category != null){
            entity.setCategory(category.strip());
        }
        entity.setExpirationDate(expirationDate);
        ProductEntity savedEntity = repository.save(entity);
        return toProduct(savedEntity);
    }

    public List<StockAdjustment> getStockHistory(long productId, int limit, int page){
        Pageable pageable = PageRequest.of(page, limit);
        return adjustmentRepository.findByProductIdOrderByIdDesc(productId, pageable).stream().map(this::toStockAdjustment).toList();
    }

    private StockAdjustment toStockAdjustment(StockAdjustmentEntity entity){
        return new StockAdjustment(entity.getId(), entity.getProductId(), entity.getChange(), entity.getResultingQuantity(), entity.getCreatedAt());
    }

    public InventorySummary getInventorySummary(int threshold){
        long productCount = repository.count();
        long totalUnits = repository.sumQuantities();
        long lowStockCount = repository.countByQuantityLessThanEqual(threshold);
        return new InventorySummary(productCount, totalUnits, lowStockCount);
    }

    public List<Product> findProductsByCategory(String category){
        List<ProductEntity> entity = repository.findByCategoryIgnoreCaseOrderByIdAsc(category.strip());
        ArrayList<Product> list = new ArrayList<>();
        for(ProductEntity p : entity){
            list.add(toProduct(p));
        }
        return list;
    }

    public List<Product> findExpiringProducts(LocalDate through){
        List<ProductEntity> entity = repository.findByExpirationDateLessThanEqualOrderByExpirationDateAscIdAsc(through);
        ArrayList<Product> list = new ArrayList<>();
        for(ProductEntity p : entity){
            list.add(toProduct(p));
        }
        return list;
    }

    @Transactional
    public Optional<Product> updateProduct(long id, String name, int quantity, String category, LocalDate expirationDate){
        Optional<ProductEntity> optionalEntity = repository.findByIdForUpdate(id);
        if(optionalEntity.isEmpty()){
            return Optional.empty();
        }
        ProductEntity entity = optionalEntity.get();
        int diff = quantity - entity.getQuantity();
        if(diff != 0){
            adjustmentRepository.save(new StockAdjustmentEntity(id, diff, quantity));
        }
        entity.setName(name.strip());
        entity.setQuantity(quantity);
        if(category != null){
            entity.setCategory(category.strip());
        }
        entity.setExpirationDate(expirationDate);
        ProductEntity updatedEntity = repository.save(entity);
        return Optional.of(toProduct(updatedEntity));
    }

    @Transactional
    public boolean deleteProduct(long id){
        Optional<ProductEntity> entity = repository.findByIdForUpdate(id);
        if (entity.isEmpty()) {
            return false;
        }
        repository.delete(entity.get());
        return true;
    }

    @Transactional
    public Optional<Product> adjustStock(long id, int change){
        Optional<ProductEntity> optionalEntity = repository.findByIdForUpdate(id);
        if(optionalEntity.isEmpty()){
            return Optional.empty();
        }
        ProductEntity entity = optionalEntity.get();
        long newQuantity = (long) entity.getQuantity() + change;
        if(newQuantity < 0 || newQuantity > Integer.MAX_VALUE){
            throw new IllegalArgumentException("Stock adjustment is out of range");
        }
        entity.setQuantity((int) newQuantity);
        ProductEntity updatedEntity = repository.save(entity);
        adjustmentRepository.save(new StockAdjustmentEntity(id, change, updatedEntity.getQuantity()));
        return Optional.of(toProduct(updatedEntity));
    }
}