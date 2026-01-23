package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.ProductPromotion;
import org.example.shared.model.entity.Product;
import org.example.shared.model.entity.Promotion;
import org.example.shared.repository.ProductPromotionRepository;
import org.example.shared.repository.ProductRepository;
import org.example.shared.repository.PromotionRepository;
import org.example.springecommerceapi.model.dto.ProductPromotionDto;
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
@RequestMapping("/api/product-promotions")
public class ProductPromotionControllerApi {

    private final ProductPromotionRepository repository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    public ProductPromotionControllerApi(ProductPromotionRepository repository, ProductRepository productRepository, PromotionRepository promotionRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.promotionRepository = promotionRepository;
    }

    @GetMapping
    public List<ProductPromotionDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPromotionDto> findById(@PathVariable Long id) {
        Optional<ProductPromotion> p = repository.findById(id);
        return p.map(pp -> ResponseEntity.ok(toDto(pp))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductPromotionDto> create(@Valid @RequestBody ProductPromotionDto dto) {
        ProductPromotion entity = toEntity(dto);
        ProductPromotion saved = repository.save(entity);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductPromotionDto> update(@PathVariable Long id, @Valid @RequestBody ProductPromotionDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ProductPromotion entity = toEntity(dto);
        entity.setId(id);
        ProductPromotion updated = repository.save(entity);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private ProductPromotionDto toDto(ProductPromotion pp) {
        return new ProductPromotionDto(pp.getId(), pp.getStartDate(), pp.getEndDate(), pp.getProduct() != null ? pp.getProduct().getId() : null, pp.getPromotion() != null ? pp.getPromotion().getId() : null);
    }

    private ProductPromotion toEntity(ProductPromotionDto dto) {
        ProductPromotion pp = new ProductPromotion();
        pp.setStartDate(dto.getStartDate());
        pp.setEndDate(dto.getEndDate());
        if (dto.getProductId() != null) {
            Optional<Product> p = productRepository.findById(dto.getProductId());
            p.ifPresent(pp::setProduct);
        }
        if (dto.getPromotionId() != null) {
            Optional<Promotion> pr = promotionRepository.findById(dto.getPromotionId());
            pr.ifPresent(pp::setPromotion);
        }
        return pp;
    }
}
