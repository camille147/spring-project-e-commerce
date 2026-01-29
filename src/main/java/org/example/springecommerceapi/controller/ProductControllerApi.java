package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Category;
import org.example.shared.model.entity.Product;
import org.example.shared.repository.CategoryRepository;
import org.example.shared.repository.ProductRepository;
import org.example.springecommerceapi.model.dto.ProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProductControllerApi {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;

    public ProductControllerApi(ProductRepository repository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/products")
    public List<ProductDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping({"/products/search/{name}"})
    public List<ProductDto> searchByName(@PathVariable String name) {
        List<Product> found = repository.findByProductNameIgnoreCaseContaining(name);
        return found.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id) {
        Optional<Product> p = repository.findById(id);
        return p.map(prod -> ResponseEntity.ok(toDto(prod))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/products")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        Product p = toEntity(dto);
        p.setIsEnabled(true);
        Product saved = repository.save(p);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/admin/products/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Product p = toEntity(dto);
        p.setId(id);
        Product updated = repository.save(p);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/admin/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Optional<Product> opt = repository.findById(id);
        if (opt.isPresent()) {
            Product p = opt.get();
            p.setIsEnabled(false);
            repository.save(p);
        }
    }

    @PatchMapping("/admin/products/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Optional<Product> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Product p = existing.get();

        if (payload.containsKey("quantity")) {
            Integer newQty = payload.get("quantity");
            if (newQty == null || newQty < 0) {
                return ResponseEntity.badRequest().body("Invalid quantity");
            }
            p.setQuantity(newQty);
        } else if (payload.containsKey("delta")) {
            Integer delta = payload.get("delta");
            if (delta == null) {
                return ResponseEntity.badRequest().body("Invalid delta");
            }
            int updatedQty = (p.getQuantity() == null ? 0 : p.getQuantity()) + delta;
            if (updatedQty < 0) updatedQty = 0;
            p.setQuantity(updatedQty);
        } else {
            return ResponseEntity.badRequest().body("Payload must contain 'quantity' or 'delta'");
        }

        Product saved = repository.save(p);
        return ResponseEntity.ok(toDto(saved));
    }

    // --- MAPPERS ---

    private ProductDto toDto(Product p) {
        List<Long> catIds = p.getCategories() != null
                ? p.getCategories().stream().map(Category::getId).collect(Collectors.toList())
                : new ArrayList<>();

        return ProductDto.builder()
                .id(p.getId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .categoryIds(catIds)
                .isEnabled(p.getIsEnabled())
                .color(p.getColor())
                .brand(p.getBrand())
                .reference(p.getReference())
                .build();
    }

    private Product toEntity(ProductDto dto) {
        Product p = new Product();
        p.setProductName(dto.getProductName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setQuantity(dto.getQuantity());
        p.setIsEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true);
        p.setColor(dto.getColor());
        p.setBrand(dto.getBrand());
        p.setReference(dto.getReference());

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
            p.setCategories(categories);
        } else {
            p.setCategories(new ArrayList<>());
        }

        return p;
    }
}