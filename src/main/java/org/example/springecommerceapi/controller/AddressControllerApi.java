package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Address;
import org.example.shared.repository.AddressRepository;
import org.example.springecommerceapi.model.dto.AddressDto;
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
@RequestMapping("/api/addresses")
public class AddressControllerApi {

    private final AddressRepository repository;

    public AddressControllerApi(AddressRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AddressDto> findAll() {
        return repository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> findById(@PathVariable Long id) {
        Optional<Address> a = repository.findById(id);
        return a.map(address -> ResponseEntity.ok(toDto(address))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AddressDto> create(@Valid @RequestBody AddressDto dto) {
        Address address = toEntity(dto);
        Address saved = repository.save(address);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> update(@PathVariable Long id, @Valid @RequestBody AddressDto dto) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Address address = toEntity(dto);
        address.setId(id);
        Address updated = repository.save(address);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private AddressDto toDto(Address a) {
        return new AddressDto(a.getId(), a.getStreet(), a.getCity(), a.getZipCode(), a.getCountry(), a.getIsActive());
    }

    private Address toEntity(AddressDto dto) {
        Address a = new Address();
        a.setStreet(dto.getStreet());
        a.setCity(dto.getCity());
        a.setZipCode(dto.getZipCode());
        a.setCountry(dto.getCountry());
        a.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return a;
    }
}
