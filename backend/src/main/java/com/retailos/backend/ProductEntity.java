package com.retailos.backend;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {
    
    @GeneratedValue(strategy = GenerationType.IDENTITY) @Id private Long id;
    @Column(nullable = false, columnDefinition = "text") private String name;
    @Column(nullable = false, columnDefinition = "text") private String category = "Uncategorized";
    @Column(nullable = false) private int quantity;

    @Column()
    private LocalDate expirationDate;


    protected ProductEntity() {}

    public ProductEntity(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCategory(){
        return category;
    }

    public LocalDate getExpirationDate(){
        return expirationDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public void setExpirationDate(LocalDate expirationDate){
        this.expirationDate = expirationDate;
    }
}
