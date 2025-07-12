package com.webbee.auth_service_webbee.controller;

import com.webbee.auth_service_webbee.exception.RoleAccessException;
import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.ExceptionResponse;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;
import com.webbee.auth_service_webbee.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user-roles")
@RequiredArgsConstructor
@Tag(name = "Управление ролями пользователя", description = "Операции, связанные с управлением ролями пользователей.")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PutMapping("/save")
    @Operation(
            summary = "Изменить роли пользователя",
            description = "Позволяет пользователю ADMIN изменять роли любого пользователя по его имени пользователя. " +
                    "Требуется роль ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Роли пользователя успешно изменены",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleStatusResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Пользователь не найден или переданные роли неправильные",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleStatusResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация.",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Недостаточно прав (пользователь, не являющийся администратором, пытается получить доступ).",
                            content = @Content(schema = @Schema(hidden = true)))
            }
    )
    public ResponseEntity<RoleStatusResponse> save(@RequestBody ChangeUserRolesRequest request) {
        RoleStatusResponse response = userRoleService.saveRoles(request);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getCode()));
    }

    @GetMapping("/{login}")
    @Operation(
            summary = "Получить роли пользователя по логину",
            description = "Получает список ролей для указанного пользователя. " +
                    "Пользователи ADMIN могут просматривать роли для любого пользователя. " +
                    "Другие аутентифицированные пользователи могут просматривать только свои собственные роли. " +
                    "Возвращает 403 Forbidden, если пользователь, не являющийся ADMIN, пытается просмотреть роли другого пользователя.",
            parameters = {
                    @Parameter(name = "login", description = "Имя пользователя, чьи роли должны быть получены.", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Роли успешно получены.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class, subTypes = String.class))),
                    @ApiResponse(responseCode = "401", description = "Требуется аутентификация.",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "403", description = "Недостаточные полномочия " +
                            "(например, пользователь, не являющийся администратором, пытается просмотреть чужие роли).",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь с указанным логином не существует.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String login,
                                                     Principal principal) {

        String authenticatedUserLogin = principal.getName();
        List<String> authenticatedUserRoles = userRoleService.getRoles(authenticatedUserLogin);

        // если есть роль админа, то мы можем получить роль любого пользователя
        if (authenticatedUserRoles.contains("ADMIN")) {
            List<String> userRoles = userRoleService.getRoles(login);
            if (userRoles.isEmpty()) {
                throw new RoleAccessException("The user with login <<" + login + ">> does not exist!", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(userRoles, HttpStatus.OK);
        } else {
            // если нет роли админа, то мы можем посмотреть только свои роли
            if (authenticatedUserLogin.equals(login)) {
                List<String> userRoles = userRoleService.getRoles(login);
                return new ResponseEntity<>(userRoles, HttpStatus.OK);
            } else {
                throw new RoleAccessException("Without an admin role, you can only view your roles!", HttpStatus.FORBIDDEN);
            }
        }
    }

}
