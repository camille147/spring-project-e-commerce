package org.example.shared.repository;

import jakarta.transaction.Transactional;
import org.example.shared.model.entity.ProductPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductPromotionRepository extends JpaRepository<ProductPromotion, Long> {

    List<ProductPromotion> findByPromotionId(Long promotionId);

    @Transactional
    void deleteByPromotionId(Long promotionId);
}