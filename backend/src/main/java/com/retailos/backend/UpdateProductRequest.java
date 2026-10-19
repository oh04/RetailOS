package com.retailos.backend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;

public record UpdateProductRequest(@NotBlank String name, @NotNull @PositiveOrZero Integer quantity){

}