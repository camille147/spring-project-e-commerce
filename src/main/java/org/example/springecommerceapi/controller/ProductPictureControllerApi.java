package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.ProductPicture;
import org.example.shared.repository.ProductPictureRepository;
import org.example.springecommerceapi.model.dto.ProductPictureDto;
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
@RequestMapping("/api/product-pictures")
public class ProductPictureControllerApi {

    private final ProductPictureRepository repository;

    public ProductPictureControllerApi(ProductPictureRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ProductPictureDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPictureDto> findById(@PathVariable Long id) {
        Optional<ProductPicture> p = repository.findById(id);
        return p.map(pic -> ResponseEntity.ok(toDto(pic))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProductPictureDto> create(@Valid @RequestBody ProductPictureDto dto) {
        ProductPicture p = toEntity(dto);
        ProductPicture saved = repository.save(p);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductPictureDto> update(@PathVariable Long id, @Valid @RequestBody ProductPictureDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ProductPicture p = toEntity(dto);
        p.setId(id);
        ProductPicture updated = repository.save(p);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private ProductPictureDto toDto(ProductPicture p) {
        return new ProductPictureDto(p.getId());
    }

    private ProductPicture toEntity(ProductPictureDto dto) {
        ProductPicture p = new ProductPicture();
        return p;
    }
}
