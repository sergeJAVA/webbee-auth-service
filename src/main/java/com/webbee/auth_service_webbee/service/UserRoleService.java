package com.webbee.auth_service_webbee.service;

import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;

import java.util.List;

public interface UserRoleService {

    RoleStatusResponse saveRoles(ChangeUserRolesRequest request);

    List<String> getRoles(String username);

}
