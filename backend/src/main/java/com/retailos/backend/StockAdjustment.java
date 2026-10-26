package com.retailos.backend;

import java.time.Instant;

public record StockAdjustment(long id, long productId, int change, int resultingQuantity, Instant createdAt){}
