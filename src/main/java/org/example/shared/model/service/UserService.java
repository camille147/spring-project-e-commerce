package org.example.shared.model.service;

import org.example.shared.entityForm.UserRegisterForm;
import org.example.shared.model.entity.User;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    public void registerUser(UserRegisterForm form) {

        User user = new User();
        user.setEmail(form.getEmail());
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setBirthDate(form.getBirthDate());

        user.setPassword(passwordEncoder.encode(form.getPassword()));

        userRepository.save(user);
    }
}
