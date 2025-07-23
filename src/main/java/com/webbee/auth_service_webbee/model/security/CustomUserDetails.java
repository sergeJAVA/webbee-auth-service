package com.webbee.auth_service_webbee.model.security;

import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@code CustomUserDetails} — это реализация интерфейса {@link UserDetails} из Spring Security.
 * <p>
 * Он предоставляет основную информацию о пользователе для фреймворка безопасности, включая имя пользователя,
 * пароль, права доступа (роли). Этот класс используется для построения объекта
 * аутентификации, когда пользователь успешно входит в систему или аутентифицируется через JWT-токен.
 * </p>
 */
public class CustomUserDetails implements UserDetails, OAuth2User {

    @Getter
    private Long id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    @Getter
    private String email;
    private Map<String, Object> attributes;

    /**
     * Создает новый экземпляр {@code CustomUserDetails}.
     *
     * @param id    ID пользователя.
     * @param username    Имя пользователя.
     * @param password    Пароль пользователя.
     * @param authorities Коллекция предоставленных прав доступа (ролей) для пользователя.
     * @param email       Адрес электронной почты пользователя.
     */
    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.email = email;
    }

    /**
     * Конструктор для OAuth2 входа
     */
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
        this.email = user.getEmail();
        this.attributes = attributes;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
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

    @Override
    public String getName() {
        return String.valueOf(attributes.get("sub"));
    }

}
