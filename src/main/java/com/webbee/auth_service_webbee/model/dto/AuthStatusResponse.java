package com.webbee.auth_service_webbee.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * {@code AuthStatusResponse} представляет структуру ответа для операций, связанных с аутентификацией,
 * таких как успешный вход или регистрация.
 * <p>
 * Он включает сообщение о статусе, код HTTP-статуса, отметку времени и JWT-токен.
 * </p>
 */
@Builder
@Getter
@Setter
@ToString
public class AuthStatusResponse {

    private String state;
    private Integer code;
    private LocalDateTime timestamp;
    private String token;

}
