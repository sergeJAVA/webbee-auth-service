package com.webbee.auth_service_webbee.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@code RegistrationRequest} представляет тело запроса для регистрации нового пользователя.
 * <p>
 * Оно включает имя пользователя, пароль и адрес электронной почты для нового пользователя.
 * Поля проверяются на пустоту и соответствие минимальным требованиям к длине.
 * </p>
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistrationRequest {

    @NotBlank(message = "The username cannot be blank")
    private String username;

    @NotBlank(message = "The password cannot be blank")
    @Size(min = 8, message = "The password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "The email cannot be blank")
    @Size(min = 8, message = "The email must be at least 8 characters long")
    private String email;

}
