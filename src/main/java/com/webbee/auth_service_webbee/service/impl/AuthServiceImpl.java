package com.webbee.auth_service_webbee.service.impl;

import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;
import com.webbee.auth_service_webbee.model.security.CustomUserDetails;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.AuthService;
import com.webbee.auth_service_webbee.service.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationProvider authenticationProvider;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthStatusResponse registration(RegistrationRequest request) {
        AuthStatusResponse response;

        // Валидация почты
        if (!isEmailValid(request.getEmail())) {
            response = AuthStatusResponse.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .state("The email <<" + request.getEmail() + ">> is not valid!")
                    .timestamp(LocalDateTime.now())
                    .build();

            return response;
        }

        // Существует ли уже такой логин
        if (isUserExist(request.getUsername())) {

            response = AuthStatusResponse.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .state("The user with this username already exist")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("The user with this username already exist");
        } else {

            // Проверка уникальности почты
            if (isEmailExist(request.getEmail())) {
                log.info("The email is already taken. Email: {}", request.getEmail());
                response = AuthStatusResponse.builder()
                        .code(HttpStatus.BAD_REQUEST.value())
                        .state("The user with such email <<" + request.getEmail() + ">> already exists")
                        .timestamp(LocalDateTime.now())
                        .build();

                return response;
            }

            // Сохраняем нового пользователя после всех проверок
            Set<Role> roles = roleRepository.findByName("USER").stream().collect(Collectors.toSet());
            userRepository.save(User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .roles(roles)
                    .build()
            );

            response = AuthStatusResponse.builder()
                    .code(HttpStatus.OK.value())
                    .state("User has been successfully registered")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("User has been successfully registered");
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthStatusResponse login(LoginRequest request) {
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        try {
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = existingUser.get().getId();
            String token = jwtService.generateJwtToken(userDetails, userId);

            return AuthStatusResponse.builder()
                    .code(HttpStatus.OK.value())
                    .state("User has been authorized")
                    .timestamp(LocalDateTime.now())
                    .token(token)
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}. Error: {}", request.getUsername(), e.getMessage(), e);
            return AuthStatusResponse.builder()
                    .code(HttpStatus.FORBIDDEN.value())
                    .state("Incorrect username or password is specified")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private boolean isUserExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private boolean isEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private boolean isEmailValid(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        int atIndex = email.indexOf('@');
        int dotIndex = email.lastIndexOf('.');
        return atIndex > 0 && dotIndex > atIndex && dotIndex < email.length() - 1;
    }

}
