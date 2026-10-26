package com.retailos.backend;

import java.time.LocalDate;

public record Product(long id, String name, int quantity, String category, LocalDate expirationDate){}