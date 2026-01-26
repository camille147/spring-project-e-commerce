package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Category;
import org.example.shared.repository.CategoryRepository;
import org.example.springecommerceapi.model.dto.CategoryDto;
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
public class CategoryControllerApi {

    private final CategoryRepository repository;

    public CategoryControllerApi(CategoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/category")
    public List<CategoryDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryDto> findById(@PathVariable Long id) {
        Optional<Category> c = repository.findById(id);
        return c.map(cat -> ResponseEntity.ok(toDto(cat))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/category")
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryDto dto) {
        Category cat = toEntity(dto);
        Category saved = repository.save(cat);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/admin/category/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Category cat = toEntity(dto);
        cat.setId(id);
        Category updated = repository.save(cat);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/admin/category/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getParentCategory() != null ? c.getParentCategory().getId() : null);
    }

    private Category toEntity(CategoryDto dto) {
        Category c = new Category();
        c.setName(dto.getName());
        if (dto.getParentCategoryId() != null) {
            Category parent = repository.findById(dto.getParentCategoryId()).orElse(null);
            c.setParentCategory(parent);
        }
        return c;
    }
}
