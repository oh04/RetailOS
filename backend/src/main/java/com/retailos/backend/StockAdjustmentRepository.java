package com.retailos.backend;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustmentEntity, Long> {
    List<StockAdjustmentEntity> findByProductIdOrderByIdDesc(long productId, Pageable pageable);
}