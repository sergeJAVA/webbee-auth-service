package com.webbee.auth_service_webbee.model.security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * {@code TokenAuthentication} расширяет {@link UsernamePasswordAuthenticationToken}
 * для представления аутентифицированного пользователя на основе JWT-токена.
 * <p>
 * Он содержит {@link TokenData}, полученные из JWT, предоставляя имя пользователя и права доступа.
 * </p>
 */
@Getter
public class TokenAuthentication extends UsernamePasswordAuthenticationToken {

    private TokenData tokenData;

    /**
     * Создает новый экземпляр {@code TokenAuthentication}.
     *
     * @param tokenData {@link TokenData}, извлеченная из JWT. Она предоставляет principal (имя пользователя)
     * и предоставленные права доступа для аутентификации.
     */
    public TokenAuthentication(TokenData tokenData) {
        super(tokenData.getUsername(), null, tokenData.getAuthorities());
        this.tokenData = tokenData;
    }

}
