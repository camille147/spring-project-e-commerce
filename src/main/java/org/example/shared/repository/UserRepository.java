package org.example.shared.repository;

import org.example.shared.model.entity.Product;
import lombok.Data;
import org.example.shared.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}