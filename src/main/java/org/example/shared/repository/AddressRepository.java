package org.example.shared.repository;

import org.example.shared.model.entity.Address;
import org.example.shared.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserAndIsActiveTrue(User user);
}