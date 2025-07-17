package com.webbee.auth_service_webbee.controller;

import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;
import com.webbee.auth_service_webbee.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Регистрация и авторизация пользователя", description = "Операции, " +
        "связанные с созданием нового пользователя и выдачей JWT, если он вошёл в систему.")
public class AuthController {

    private final AuthService authService;

    @PutMapping("/signup")
    @Operation(summary = "Для регистрации нового пользователя", description = "Логин и почта должны быть уникальными")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Переданная почта невалидная," +
                    " переданная почта уже занята, переданный логин уже занят",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthStatusResponse.class))),
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрировался",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthStatusResponse.class))),
    })
    public ResponseEntity<AuthStatusResponse> signUp(@Valid @RequestBody RegistrationRequest request) {
        AuthStatusResponse response = authService.registration(request);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getCode()));
    }

    @PostMapping("/signin")
    @Operation(summary = "Для входа в систему и выдачи JWT", description = "Для входа в систему нужно передать логин и пароль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно авторизовался",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthStatusResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не удалось авторизоваться",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthStatusResponse.class)))
    })
    public ResponseEntity<AuthStatusResponse> signIn(@Valid @RequestBody LoginRequest request) {
        AuthStatusResponse response = authService.login(request);

        if (response.getCode() == HttpStatus.OK.value()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + response.getToken());
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

}
