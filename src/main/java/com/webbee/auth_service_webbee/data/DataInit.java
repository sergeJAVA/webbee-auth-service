package com.webbee.auth_service_webbee.data;

import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * {@code DataInit} — это компонент, отвечающий за инициализацию данных
 * пользователя в базе данных при запуске приложения.
 */
@Component
@RequiredArgsConstructor
public class DataInit {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostConstruct
    void init() {
        Optional<Role> role = roleRepository.findByName("ADMIN");

        Optional<User> user = userRepository.findByUsername("ADMIN");

        if (user.isPresent()) {
            return;
        }

        if (role.isPresent()) {
            Role rl = role.get();
            Set<Role> roles = new HashSet<>();
            roles.add(rl);
            User admin = User.builder()
                    .username("ADMIN")
                    .password(passwordEncoder.encode("password123"))
                    .email("admin@yandex.ru")
                    .roles(roles)
                    .build();
            userRepository.save(admin);
        }
    }

}
