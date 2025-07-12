package com.webbee.auth_service_webbee.controller;

import com.webbee.auth_service_webbee.exception.RoleAccessException;
import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;
import com.webbee.auth_service_webbee.service.UserRoleService;
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
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PutMapping("/save")
    public ResponseEntity<RoleStatusResponse> save(@RequestBody ChangeUserRolesRequest request) {
        RoleStatusResponse response = userRoleService.saveRoles(request);
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getCode()));
    }

    @GetMapping("/{login}")
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
