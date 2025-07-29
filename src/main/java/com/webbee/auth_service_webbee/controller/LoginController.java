package com.webbee.auth_service_webbee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Tag(name = "Login Controller", description = "Обработка логина, OAuth2")
public class LoginController {

    @Operation(
            summary = "Возвращает HTML форму логина",
            description = "Эта ручка отдает HTML-страницу с формой логина"
    )
    @GetMapping("/loginForm")
    public String loginPage() {
        return "login.html";
    }

    @Operation(
            summary = "Успешный вход",
            description = "Возвращает сообщение об успешной авторизации вместе с JWT токеном",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @ResponseBody
    @GetMapping("/success-page")
    public ResponseEntity<String> success(
            @Parameter(description = "JWT токен в формате Bearer", required = true)
            @RequestHeader("Authorization") String headerAuth) {
        String token = headerAuth.substring(7);
        return ResponseEntity.ok("Success login. Token: " + token);
    }

    @Operation(
            summary = "Ошибка регистрации",
            description = "Сообщение, что пользователь уже зарегистрирован локально",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ошибка регистрации",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @ResponseBody
    @GetMapping("/failure-page")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("User with this email address is already registered locally.");
    }

    @Operation(
            summary = "OAuth2 Callback",
            description = "Обработка успешной авторизации через Google OAuth2",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная авторизация через Google",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    @ResponseBody
    @GetMapping("/oauth2/callback")
    public ResponseEntity<String> callback(@RequestParam String token) {
        return ResponseEntity.ok("Success login with Google. Token: " + token);
    }

}
