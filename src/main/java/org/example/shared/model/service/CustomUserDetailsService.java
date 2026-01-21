package org.example.shared.model.service;

import lombok.Setter;
import org.example.shared.model.entity.User;
import org.example.shared.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) //username=email
                .password(user.getPassword())
                .roles("USER") // modifier pour le role qui est enum(àfaire)
                .build();
    }
}
