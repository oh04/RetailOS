package com.retailos.backend;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateProductRequest(@NotBlank String name, @NotNull @PositiveOrZero Integer quantity, @Pattern(regexp = "(?s).*\\S.*", message = "Category must not be blank") String category, LocalDate expirationDate){

}