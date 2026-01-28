package org.example.shared.repository;

import org.example.shared.model.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE " +
            ":search IS NULL OR :search = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Promotion> searchByName(@Param("search") String search, Pageable pageable);
}