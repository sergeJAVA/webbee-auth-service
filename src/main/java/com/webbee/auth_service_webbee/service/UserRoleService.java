package com.webbee.auth_service_webbee.service;

import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;
import com.webbee.auth_service_webbee.model.dto.UserDto;

public interface UserRoleService {

    RoleStatusResponse saveRoles(ChangeUserRolesRequest request);

    UserDto getRoles(String username);

}
