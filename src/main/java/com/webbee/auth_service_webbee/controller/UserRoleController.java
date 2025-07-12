package com.webbee.auth_service_webbee.controller;

import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;
import com.webbee.auth_service_webbee.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
