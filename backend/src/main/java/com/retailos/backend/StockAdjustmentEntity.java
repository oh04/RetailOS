package com.retailos.backend;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "stock_adjustments")
public class StockAdjustmentEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false) private long productId;
    @Column(name = "quantity_change", nullable = false) private int change;
    @Column(name = "resulting_quantity", nullable = false) private int resultingQuantity;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected StockAdjustmentEntity() {}

    public StockAdjustmentEntity(long productId, int change, int resultingQuantity) {
        this.productId = productId;
        this.change = change;
        this.resultingQuantity = resultingQuantity;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public long getProductId() {
        return productId;
    }

    public int getChange() {
        return change;
    }

    public int getResultingQuantity() {
        return resultingQuantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
