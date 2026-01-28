package org.example.shared.repository;

import org.example.shared.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.productPromotions pp " +
            "LEFT JOIN FETCH pp.promotion prom " +
            "WHERE p.id = :id AND (pp.startDate <= CURRENT_TIMESTAMP AND (pp.endDate IS NULL OR pp.endDate >= CURRENT_TIMESTAMP))")
    Optional<Product> findProductWithActivePromotions(@Param("id") Long id);
    List<Product> findByProductNameIgnoreCaseContaining(String name);

    @Query(value = "SELECT p.* FROM product p LEFT JOIN order_line ol ON ol.product_id = p.id GROUP BY p.id ORDER BY COUNT(ol.id) DESC", nativeQuery = true)
    List<Product> findBestProducts();
}