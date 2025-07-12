package com.webbee.auth_service_webbee.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code ExceptionResponse} представляет общую структуру ответа об ошибке, возвращаемую API.
 * <p>
 * Она включает сообщение об ошибке и связанный с ней код HTTP-статуса.
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {

    private String message;
    private int code;

}
