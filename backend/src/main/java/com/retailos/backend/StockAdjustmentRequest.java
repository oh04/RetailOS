package com.retailos.backend;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(@NotNull Integer change) {}