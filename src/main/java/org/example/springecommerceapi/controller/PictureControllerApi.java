package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Picture;
import org.example.shared.model.entity.Product;
import org.example.shared.repository.PictureRepository;
import org.example.shared.repository.ProductRepository;
import org.example.springecommerceapi.model.dto.PictureDto;
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
@RequestMapping("/api")
public class PictureControllerApi {

    private final PictureRepository repository;
    private final ProductRepository productRepository;

    public PictureControllerApi(PictureRepository repository, ProductRepository productRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
    }

    @GetMapping("/admin/pictures/{id}")
    public ResponseEntity<PictureDto> findById(@PathVariable Long id) {
        Optional<Picture> p = repository.findById(id);
        return p.map(pic -> ResponseEntity.ok(toDto(pic))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/products/{productId}/pictures")
    public ResponseEntity<List<PictureDto>> listForProduct(@PathVariable Long productId) {
        Optional<Product> maybe = productRepository.findById(productId);
        if (maybe.isEmpty()) return ResponseEntity.notFound().build();
        Product product = maybe.get();
        List<PictureDto> dtos = product.getGallery() == null ? List.of() : product.getGallery().stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/admin/products/{productId}/pictures")
    public ResponseEntity<PictureDto> createForProduct(@PathVariable Long productId, @Valid @RequestBody PictureDto dto) {
        Optional<Product> maybe = productRepository.findById(productId);
        if (maybe.isEmpty()) return ResponseEntity.badRequest().build();
        Product product = maybe.get();

        Picture p = toEntity(dto);
        Picture saved = repository.save(p);

        var gallery = product.getGallery();
        if (gallery == null) gallery = new java.util.ArrayList<>();
        gallery.add(saved);
        product.setGallery(gallery);
        productRepository.save(product);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/admin/pictures/{id}")
    public ResponseEntity<PictureDto> update(@PathVariable Long id, @Valid @RequestBody PictureDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Picture p = toEntity(dto);
        p.setId(id);
        Picture updated = repository.save(p);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/admin/pictures/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Optional<Picture> maybe = repository.findById(id);
        maybe.ifPresent(pic -> {
            List<Product> products = productRepository.findAll().stream().filter(prod -> prod.getGallery() != null && prod.getGallery().contains(pic)).collect(Collectors.toList());
            for (Product prod : products) {
                prod.getGallery().remove(pic);
                productRepository.save(prod);
            }
            repository.deleteById(id);
        });
    }

    private PictureDto toDto(Picture p) {
        return new PictureDto(p.getId(), p.getName(), p.getPictureUrl(), p.getIsActive());
    }

    private Picture toEntity(PictureDto dto) {
        Picture p = new Picture();
        p.setName(dto.getName());
        p.setPictureUrl(dto.getPictureUrl());
        p.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return p;
    }
}
