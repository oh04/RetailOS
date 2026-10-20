package com.retailos.backend;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByOrderByIdAsc();
    List<ProductEntity> findByQuantityLessThanEqualOrderByIdAsc(int threshold);
    List<ProductEntity> findByNameContainingIgnoreCaseOrderByIdAsc(String name);
}