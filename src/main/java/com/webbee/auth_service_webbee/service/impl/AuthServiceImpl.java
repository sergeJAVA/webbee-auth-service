package com.webbee.auth_service_webbee.service.impl;

import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;
import com.webbee.auth_service_webbee.model.security.CustomUserDetails;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationProvider authenticationProvider;

    @Override
    public AuthStatusResponse registration(RegistrationRequest request) {
        AuthStatusResponse response;

        if (isUserExist(request.getUsername())) {

            response = AuthStatusResponse.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .state("The user with this username already exist")
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("The user with this username already exist");
        } else {

            userRepository.save(User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .roles(Set.of("USER"))
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
    public AuthStatusResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            return AuthStatusResponse.builder()
                    .code(HttpStatus.OK.value())
                    .state("User has been authorized")
                    .timestamp(LocalDateTime.now())
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

}
