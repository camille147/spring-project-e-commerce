package org.example.shared.model.service;

import org.example.shared.model.entity.User;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    public void registerUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword()); //hashage mdp
        user.setPassword(encodedPassword);

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }

        userRepository.save(user);
    }
}
