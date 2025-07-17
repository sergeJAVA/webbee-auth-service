package com.webbee.auth_service_webbee.model.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * {@code CustomUserDetails} — это реализация интерфейса {@link UserDetails} из Spring Security.
 * <p>
 * Он предоставляет основную информацию о пользователе для фреймворка безопасности, включая имя пользователя,
 * пароль, права доступа (роли). Этот класс используется для построения объекта
 * аутентификации, когда пользователь успешно входит в систему или аутентифицируется через JWT-токен.
 * </p>
 */
public class CustomUserDetails implements UserDetails {

    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    @Getter
    private String email;

    /**
     * Создает новый экземпляр {@code CustomUserDetails}.
     *
     * @param username    Имя пользователя.
     * @param password    Пароль пользователя.
     * @param authorities Коллекция предоставленных прав доступа (ролей) для пользователя.
     * @param email       Адрес электронной почты пользователя.
     */
    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, String email) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
