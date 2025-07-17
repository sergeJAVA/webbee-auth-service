package com.webbee.auth_service_webbee.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * {@code TokenUtil} — это вспомогательный класс, предназначенный для работы с токенами,
 * в частности для извлечения JWT-токенов из заголовков HTTP-запросов.
 * <p>
 * Этот класс является {@code final} и имеет приватный конструктор, что делает его
 * утилитарным классом, содержащим только статические методы.
 * </p>
 */
public final class TokenUtil {

    private TokenUtil() {

    }

    public static String parseToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        return StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : null;
    }

}
