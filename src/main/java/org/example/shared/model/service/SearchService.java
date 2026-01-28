package org.example.shared.model.service;

import org.example.shared.model.entity.Product;
import org.example.shared.repository.ProductRepository;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    @Autowired private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    // search pour le shop
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) return productRepository.findAll();
        return productRepository.findByProductNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query);
    }

    // recherche pour le Dashboard
    public Map<String, Object> adminGlobalSearch(String query) {
        Map<String, Object> results = new HashMap<>();
        results.put("products", productRepository.findByProductNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query));
        results.put("users", userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, query));
        return results;
    }
}