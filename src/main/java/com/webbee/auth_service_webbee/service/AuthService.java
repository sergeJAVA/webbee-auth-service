package com.webbee.auth_service_webbee.service;

import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;

public interface AuthService {

    AuthStatusResponse registration(RegistrationRequest request);

    AuthStatusResponse login(LoginRequest request);

}
