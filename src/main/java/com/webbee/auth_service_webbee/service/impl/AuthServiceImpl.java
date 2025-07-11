package com.webbee.auth_service_webbee.service.impl;

import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;
import com.webbee.auth_service_webbee.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthStatusResponse registration(RegistrationRequest request) {
        return null;
    }

    @Override
    public AuthStatusResponse login(LoginRequest request) {
        return null;
    }

}
