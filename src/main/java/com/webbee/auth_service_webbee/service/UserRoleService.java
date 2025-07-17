package com.webbee.auth_service_webbee.service;

import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;

import java.util.List;

/**
 * {@code UserRoleService} определяет контракт для управления ролями пользователей.
 */
public interface UserRoleService {

    /**
     * {@inheritDoc}
     * Обновляет роли для указанного пользователя.
     *
     * @param request {@link ChangeUserRolesRequest}, содержащий имя пользователя
     * и новый набор ролей для назначения.
     * @return {@link RoleStatusResponse}, указывающий на результат операции изменения ролей.
     */
    RoleStatusResponse saveRoles(ChangeUserRolesRequest request);

    /**
     * {@inheritDoc}
     * Извлекает список названий ролей для заданного имени пользователя.
     *
     * @param username Имя пользователя, чьи роли должны быть извлечены.
     * @return {@link List} из {@link String}, представляющий названия ролей,
     * назначенных пользователю. Возвращает пустой список, если пользователь не найден.
     */
    List<String> getRoles(String username);

}
