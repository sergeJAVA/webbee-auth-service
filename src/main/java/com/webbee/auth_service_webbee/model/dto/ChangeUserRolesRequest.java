package com.webbee.auth_service_webbee.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * {@code ChangeUserRolesRequest} представляет тело запроса для изменения ролей пользователя.
 * <p>
 * Он включает имя пользователя, для которого нужно изменить роли, и набор ролей для назначения.
 * </p>
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeUserRolesRequest {

    private String username;
    private Set<String> roles;

}
