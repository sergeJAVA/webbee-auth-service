package com.webbee.auth_service_webbee.controller;

import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;
import com.webbee.auth_service_webbee.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PutMapping("/signup")
    public ResponseEntity<AuthStatusResponse> signUp(@Valid @RequestBody RegistrationRequest request) {
        AuthStatusResponse response = authService.registration(request);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getCode()));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthStatusResponse> signIn(@Valid @RequestBody LoginRequest request) {
        AuthStatusResponse response = authService.login(request);

        if (response.getCode() == HttpStatus.OK.value()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + response.getToken());
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @GetMapping("/hello")
    public String hello() {
        return "HELLO WORLD!";
    }

}
