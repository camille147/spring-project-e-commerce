package org.example.shared.repository;

import org.example.shared.model.entity.Order;
import org.example.shared.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query(value = "SELECT EXTRACT(DAY FROM o.created_at) as day, COUNT(o.id) as cnt FROM orders o WHERE EXTRACT(MONTH FROM o.created_at) = :month AND EXTRACT(YEAR FROM o.created_at) = :year GROUP BY day ORDER BY day", nativeQuery = true)
    List<Object[]> findOrderCountByMonthYear(@Param("month") int month, @Param("year") int year);

}