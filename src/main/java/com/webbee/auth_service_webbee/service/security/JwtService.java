package com.webbee.auth_service_webbee.service.security;

import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.security.CustomUserDetails;
import com.webbee.auth_service_webbee.model.security.TokenData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@code JwtService} — это сервисный класс, отвечающий за создание, парсинг и валидацию JWT-токенов.
 * <p>
 * Он использует секретный ключ и время жизни, настроенные через свойства приложения.
 * </p>
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Секретный ключ, используемый для подписи и проверки JWT-токенов.
     * Получается из свойства 'jwt.secret'.
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Время жизни JWT-токена в миллисекундах.
     * Получается из свойства 'jwt.life-time'.
     */
    @Value("${jwt.life-time}")
    private Long lifeTime;

    /**
     * Извлекает ID пользователя из JWT-токена.
     *
     * @param token JWT-токен.
     * @return ID пользователя в формате {@link Long}.
     */
    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Извлекает имя пользователя (subject) из JWT-токена.
     *
     * @param token JWT-токен.
     * @return Имя пользователя в формате {@link String}.
     */
    public String getUserNameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Извлекает список ролей из JWT-токена.
     *
     * @param token JWT-токен.
     * @return Список названий ролей в формате {@link List<String>}.
     */
    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, (Function<Claims, List<String>>) claims -> claims.get("roles", List.class));
    }

    /**
     * Универсальный метод для извлечения определенного клейма (claim) из JWT-токена.
     *
     * @param token         JWT-токен.
     * @param claimResolver Функция, которая принимает {@link Claims} и возвращает требуемое клеймо.
     * @param <T>           Тип возвращаемого клейма.
     * @return Значение клейма.
     */
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(getAllClaimsFromToken(token));
    }

    /**
     * Извлекает все клеймы (claims) из JWT-токена.
     * <p>
     * Использует секретный ключ для проверки подписи токена.
     * </p>
     *
     * @param token JWT-токен.
     * @return Объект {@link Claims}, содержащий все клеймы токена.
     * @throws IllegalArgumentException Если передан неверный аргумент (например, токен недействителен).
     * @throws JwtException             Если произошла ошибка при парсинге или проверке токена.
     */
    @SneakyThrows
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error! Wrong argument passed!");
        }

    }

    /**
     * Парсит JWT-токен и преобразует его в объект {@link TokenData}.
     *
     * @param token JWT-токен для парсинга.
     * @return Объект {@link TokenData}, содержащий ID пользователя, имя пользователя, сам токен
     * и список прав доступа.
     */
    public TokenData parseToken(String token) {
        return TokenData.builder()
                .token(token)
                .username(getUserNameFromToken(token))
                .authorities(getRolesFromToken(token).stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()))
                .id(getUserIdFromToken(token))
                .build();
    }

    /**
     * Проверяет, истек ли срок действия JWT-токена.
     *
     * @param token JWT-токен для проверки.
     * @return {@code true}, если срок действия токена истек, иначе {@code false}.
     * Также возвращает {@code true} в случае ошибок парсинга или других JWT-исключений.
     */
    public boolean isTokenExpired(String token) {
        try {
            return getAllClaimsFromToken(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error while parsing token for expiration check: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Генерирует новый JWT-токен для указанного пользователя.
     * <p>
     * Токен содержит имя пользователя (subject), ID пользователя и список ролей в качестве клеймов,
     * а также информацию о дате выдачи и сроке действия.
     * </p>
     *
     * @param userDetails Объект {@link CustomUserDetails}, содержащий данные пользователя.
     * @param userId      ID пользователя.
     * @return Сгенерированный JWT-токен в формате {@link String}.
     */
    public String generateJwtToken(CustomUserDetails userDetails, Long userId) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("username", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet()));
        claims.put("userId", userId);
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + lifeTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String generateJwtToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles().stream()
                .map(Role::getName)
                .map(role -> "ROLE_" + role)
                .collect(Collectors.toSet()));
        claims.put("userId", user.getId());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + lifeTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

}
