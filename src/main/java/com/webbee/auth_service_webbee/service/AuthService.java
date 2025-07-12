package com.webbee.auth_service_webbee.service;

import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;

/**
 * {@code AuthService} определяет контракт для сервисов аутентификации и регистрации пользователей.
 */
public interface AuthService {

    /**
     * {@inheritDoc}
     * Обрабатывает регистрацию нового пользователя.
     *
     * @param request {@link RegistrationRequest}, содержащий данные нового пользователя.
     * @return {@link AuthStatusResponse}, указывающий на результат регистрации,
     * включая сообщение о статусе, код, отметку времени и потенциально JWT-токен.
     */
    AuthStatusResponse registration(RegistrationRequest request);

    /**
     * {@inheritDoc}
     * Обрабатывает вход существующего пользователя.
     *
     * @param request {@link LoginRequest}, содержащий учетные данные пользователя.
     * @return {@link AuthStatusResponse}, указывающий на результат входа,
     * включая сообщение о статусе, код, отметку времени и JWT-токен при успехе.
     */
    AuthStatusResponse login(LoginRequest request);

}
