package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Promotion;
import org.example.shared.repository.PromotionRepository;
import org.example.springecommerceapi.model.dto.PromotionDto;
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
@RequestMapping("/api/promotions")
public class PromotionControllerApi {

    private final PromotionRepository repository;

    public PromotionControllerApi(PromotionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<PromotionDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionDto> findById(@PathVariable Long id) {
        Optional<Promotion> p = repository.findById(id);
        return p.map(prom -> ResponseEntity.ok(toDto(prom))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PromotionDto> create(@Valid @RequestBody PromotionDto dto) {
        Promotion p = toEntity(dto);
        Promotion saved = repository.save(p);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionDto> update(@PathVariable Long id, @Valid @RequestBody PromotionDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Promotion p = toEntity(dto);
        p.setId(id);
        Promotion updated = repository.save(p);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private PromotionDto toDto(Promotion p) {
        return new PromotionDto(p.getId(), p.getName(), p.getDiscountRate());
    }

    private Promotion toEntity(PromotionDto dto) {
        Promotion p = new Promotion();
        p.setName(dto.getName());
        p.setDiscountRate(dto.getDiscountRate());
        return p;
    }
}
