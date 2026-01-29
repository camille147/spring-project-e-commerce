package org.example.shared.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.shared.model.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> filterProducts(String keyword, List<Long> categoryIds, List<String> colors, Double maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("productName")), searchPattern)//,
                        //cb.like(cb.lower(root.get("brand")), searchPattern)
                ));
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.join("categories").get("id").in(categoryIds));
            }

            if (colors != null && !colors.isEmpty()) {
                predicates.add(root.get("color").in(colors));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
