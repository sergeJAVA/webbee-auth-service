package com.webbee.auth_service_webbee.model.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * {@code TokenData} — это объект передачи данных (DTO), который инкапсулирует важную
 * информацию, полученную из JWT-токена.
 * <p>
 * Он включает ID пользователя, имя пользователя, строку токена и его предоставленные права доступа (роли).
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenData {

    private Long id;
    private String username;
    private String token;
    private List<? extends GrantedAuthority> authorities;

}
