package org.example.shared.repository;

import org.example.shared.model.entity.Order;
import org.example.shared.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT DAY(o.createdAt) as day, COUNT(o) as count " +
            "FROM Order o " +
            "WHERE MONTH(o.createdAt) = :month " +
            "AND YEAR(o.createdAt) = :year " +
            "GROUP BY DAY(o.createdAt)")
    List<Object[]> findOrderCountByMonthYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT SUM(o.total) FROM Order o")
    Double sumTotalAmount();

    @Query("SELECT o FROM Order o " +
            "WHERE (:client IS NULL OR (LOWER(o.user.firstName) LIKE LOWER(CONCAT('%', :client, '%')) " +
            "OR LOWER(o.user.lastName) LIKE LOWER(CONCAT('%', :client, '%')) " +
            "OR LOWER(o.user.email) LIKE LOWER(CONCAT('%', :client, '%')))) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:number IS NULL OR o.orderNumber LIKE CONCAT('%', :number, '%')) " +
            "AND (:date IS NULL OR CAST(o.createdAt AS date) = :date)")
    Page<Order> searchOrders(@Param("client") String client,
                             @Param("status") Integer status,
                             @Param("number") String number,
                             @Param("date") LocalDate date,
                             Pageable pageable);
}