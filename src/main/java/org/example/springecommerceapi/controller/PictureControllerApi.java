package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Picture;
import org.example.shared.repository.PictureRepository;
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
@RequestMapping("/api/pictures")
public class PictureControllerApi {

    private final PictureRepository repository;

    public PictureControllerApi(PictureRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<PictureDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PictureDto> findById(@PathVariable Long id) {
        Optional<Picture> p = repository.findById(id);
        return p.map(pic -> ResponseEntity.ok(toDto(pic))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PictureDto> create(@Valid @RequestBody PictureDto dto) {
        Picture p = toEntity(dto);
        Picture saved = repository.save(p);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PictureDto> update(@PathVariable Long id, @Valid @RequestBody PictureDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Picture p = toEntity(dto);
        p.setId(id);
        Picture updated = repository.save(p);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
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
