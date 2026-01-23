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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductControllerApi {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;

    public ProductControllerApi(ProductRepository repository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<ProductDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(@PathVariable Long id) {
        Optional<Product> p = repository.findById(id);
        return p.map(prod -> ResponseEntity.ok(toDto(prod))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        Product p = toEntity(dto);
        Product saved = repository.save(p);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Product p = toEntity(dto);
        p.setId(id);
        Product updated = repository.save(p);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private ProductDto toDto(Product p) {
        return new ProductDto(p.getId(), p.getProductName(), p.getDescription(), p.getPrice(), p.getQuantity(), p.getCategory() != null ? p.getCategory().getId() : null, p.getIsEnabled(), p.getColor(), p.getBrand(), p.getReference());
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
        if (dto.getCategoryId() != null) {
            Category c = categoryRepository.findById(dto.getCategoryId()).orElse(null);
            p.setCategory(c);
        }
        return p;
    }
}
