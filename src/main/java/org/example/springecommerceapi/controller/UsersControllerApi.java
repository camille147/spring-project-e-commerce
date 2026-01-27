package org.example.springecommerceapi.controller;

import org.example.shared.model.entity.User;
import org.example.shared.repository.UserRepository;
import org.example.springecommerceapi.model.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UsersControllerApi {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersControllerApi(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/users")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/admin/get={id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(u -> ResponseEntity.ok(toDto(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        User toSave = toEntity(userDto);

        String rawPassword = userDto.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        toSave.setPassword(passwordEncoder.encode(rawPassword));

        User saved = userRepository.save(toSave);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(toDto(saved));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User u = existing.get();
        u.setFirstName(userDto.getFirstName());
        u.setLastName(userDto.getLastName());
        u.setEmail(userDto.getEmail());
        u.setBirthDate(userDto.getBirthDate());

        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updated = userRepository.save(u);
        return ResponseEntity.ok(toDto(updated));
    }


    @GetMapping("/user/profile")
    public ResponseEntity<UserDto> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(userOpt.get()));
    }

    @PutMapping("/user/profile")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = auth.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        String newEmail = userDto.getEmail();
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                return ResponseEntity.badRequest().body("Error: Email is already taken!");
            }
            user.setEmail(newEmail);
        }

        if (userDto.getFirstName() != null) user.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) user.setLastName(userDto.getLastName());
        if (userDto.getBirthDate() != null) user.setBirthDate(userDto.getBirthDate());

        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
            userDto.setIsActivated(true);

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toDto(saved));
    }

    @PatchMapping("/user/password/id={id}")
    public ResponseEntity<?> changeUserPassword(@PathVariable Long id, @RequestBody String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok("Password updated successfully");
    }


    public UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getBirthDate(), null, u.getIsActivated());
    }

    public User toEntity(UserDto dto) {
        User u = new User();
        u.setFirstName(dto.getFirstName());
        u.setLastName(dto.getLastName());
        u.setEmail(dto.getEmail());
        u.setBirthDate(dto.getBirthDate());
        u.setIsActivated(dto.getIsActivated());
        return u;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(Long id) {
            super("User not found: " + id);
        }
    }
}
