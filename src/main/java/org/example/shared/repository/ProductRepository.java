package org.example.shared.repository;

import org.example.shared.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.productPromotions pp " +
            "LEFT JOIN FETCH pp.promotion prom " +
            "WHERE p.id = :id AND p.isEnabled = true " +
            "AND (pp.startDate <= CURRENT_TIMESTAMP AND (pp.endDate IS NULL OR pp.endDate >= CURRENT_TIMESTAMP))")
    Optional<Product> findProductWithActivePromotions(@Param("id") Long id);

    List<Product> findByProductNameIgnoreCaseContaining(String name);

    @Query("SELECT DISTINCT p.color FROM Product p WHERE p.color IS NOT NULL AND p.isEnabled = true")
    List<String> findAllDistinctColors();

    List<Product> findByProductNameContainingIgnoreCaseOrBrandContainingIgnoreCase(String name, String brand);

    List<Product> findByProductNameContainingIgnoreCase(String keyword);

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.categories c " +
            "WHERE p.isEnabled = true " +
            "AND (:search IS NULL OR :search = '' OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:categoryId IS NULL OR c.id = :categoryId)")
    Page<Product> searchAndFilter(
            @Param("search") String search,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Query(value = "SELECT p.* FROM product p " +
            "LEFT JOIN order_line ol ON ol.product_id = p.id " +
            "WHERE p.is_enabled = true " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(ol.id) DESC", nativeQuery = true)
    List<Product> findBestProducts();
}