package org.example.shared.repository;

import org.example.shared.model.entity.Product;
import lombok.Data;
import org.example.shared.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Utilisé par le CustomUserDetailsService pour l'authentification.
     * @param email L'identifiant saisi dans le formulaire de login.
     * @return Un Optional contenant l'utilisateur s'il existe.
     */
    Optional<User> findByEmail(String email);

    /**
     * Optionnel : utile pour vérifier si un email est déjà pris lors du signup.
     */
    Boolean existsByEmail(String email);
}