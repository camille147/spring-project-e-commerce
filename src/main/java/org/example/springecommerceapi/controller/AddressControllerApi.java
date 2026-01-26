package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.Address;
import org.example.shared.model.entity.User;
import org.example.shared.repository.AddressRepository;
import org.example.shared.repository.UserRepository;
import org.example.springecommerceapi.model.dto.AddressDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AddressControllerApi {

    private final AddressRepository repository;
    private final UserRepository userRepository;

    public AddressControllerApi(AddressRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<AddressDto> findById(@PathVariable Long id) {
        Optional<Address> a = repository.findById(id);
        if (a.isEmpty()) return ResponseEntity.notFound().build();

        Address address = a.get();
        if (isNotOwnerNorAdmin(address.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(toDto(address));
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDto> create(@Valid @RequestBody AddressDto dto) {
        User current = getAuthenticatedUser();
        if (current == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Address address = toEntity(dto);
        address.setUser(current);

        Address saved = repository.save(address);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressDto> update(@PathVariable Long id, @Valid @RequestBody AddressDto dto) {
        Optional<Address> existing = repository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        Address address = existing.get();
        if (isNotOwnerNorAdmin(address.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        if (dto.getIsActive() != null) address.setIsActive(dto.getIsActive());

        Address updated = repository.save(address);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private boolean isOwnerOrAdmin(User owner) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return false;
        String email = auth.getName();


        if (owner == null) return false;
        return email.equals(owner.getEmail());
    }

    private boolean isNotOwnerNorAdmin(User owner) {
        return !isOwnerOrAdmin(owner);
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
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
