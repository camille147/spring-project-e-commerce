package org.example.shared.repository;

import org.example.shared.model.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    @Query(value = "SELECT ol.product_id as productId, SUM(ol.quantity) as salesCount FROM order_line ol GROUP BY ol.product_id ORDER BY salesCount DESC", nativeQuery = true)
    List<Object[]> findSalesCountGroupedByProduct();
}