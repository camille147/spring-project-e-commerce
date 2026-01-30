package org.example.shared.model.service;

import org.example.shared.model.entity.Category;
import org.example.shared.model.entity.Product;
import org.example.shared.repository.CategoryRepository;
import org.example.shared.repository.ProductRepository;
import org.example.shared.repository.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.data.domain.Pageable;

@Service
public class ShopService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    //Récupère les produits filtrés et paginés
    public Page<Product> getFilteredProducts(String keyword, List<Long> categoryIds, List<String> colors, Double price, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filterProducts(keyword, categoryIds, colors, price);
        return productRepository.findAll((Specification<Product>) spec, pageable);
    }

    //Récupère toutes les catégories pour les filtres
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    //Récupère les attributs distincts pour les filtres latéraux
    public List<String> getAvailableColors() {
        return productRepository.findAllDistinctColors();
    }

    public List<String> getAvailableBrands() {
        return productRepository.findAllDistinctBrands();
    }
}
