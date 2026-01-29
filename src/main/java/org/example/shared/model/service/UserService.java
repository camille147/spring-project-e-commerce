package org.example.shared.model.service;

import jakarta.transaction.Transactional;
import org.example.shared.entityForm.UserRegisterForm;
import org.example.shared.model.entity.User;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        user.setPrivacyConsent(true);
        user.setConsentDate(LocalDateTime.now());

        user.setPassword(passwordEncoder.encode(form.getPassword()));

        userRepository.save(user);
    }


    @Transactional
    public void deleteUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setFirstName("Utilisateur");
        user.setLastName("Supprimé");
        user.setEmail("deleted_" + userId + "_" + System.currentTimeMillis() + "@techzone.fr");
        user.setPassword("DELETED_ACCOUNT_" + System.currentTimeMillis());
        user.setBirthDate(LocalDate.of(1900, 1, 1));

        user.setIsActivated(false);
        user.setPrivacyConsent(false);
        user.setDeletedAt(LocalDateTime.now());


        if (user.getAddresses() != null) {
            user.getAddresses().forEach(addr -> addr.setIsActive(false));
        }

        userRepository.save(user);
    }
}
