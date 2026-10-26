package com.retailos.backend;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByOrderByIdAsc();
    List<ProductEntity> findByQuantityLessThanEqualOrderByIdAsc(int threshold);
    List<ProductEntity> findByNameContainingIgnoreCaseOrderByIdAsc(String name);
    List<ProductEntity> findByCategoryIgnoreCaseOrderByIdAsc(String category);
    List<ProductEntity> findByExpirationDateLessThanEqualOrderByExpirationDateAscIdAsc(LocalDate through);

    long countByQuantityLessThanEqual(int threshold);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductEntity p where p.id = :id")
    Optional<ProductEntity> findByIdForUpdate(@Param("id") long id);


    @Query ("select coalesce(sum(p.quantity), 0) from ProductEntity p")
    public long sumQuantities();
}