package org.example.shared.repository;

import org.example.shared.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.productPromotions pp " +
            "LEFT JOIN FETCH pp.promotion prom " +
            "WHERE p.id = :id AND (pp.startDate <= CURRENT_TIMESTAMP AND (pp.endDate IS NULL OR pp.endDate >= CURRENT_TIMESTAMP))")
    Optional<Product> findProductWithActivePromotions(@Param("id") Long id);

    List<Product> findByProductNameIgnoreCaseContaining(String name);


    @Query("SELECT DISTINCT p.color FROM Product p WHERE p.color IS NOT NULL")
    List<String> findAllDistinctColors();

    List<Product> findByProductNameContainingIgnoreCaseOrBrandContainingIgnoreCase(String name, String brand);

    List<Product> findByProductNameContainingIgnoreCase(String keyword);

}