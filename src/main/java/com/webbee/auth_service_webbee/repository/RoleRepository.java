package com.webbee.auth_service_webbee.repository;

import com.webbee.auth_service_webbee.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * {@code RoleRepository} — это интерфейс репозитория Spring Data JPA для сущности {@link Role}.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит сущность {@link Role} по ее имени.
     *
     * @param name Имя роли для поиска.
     * @return {@link Optional}, содержащий найденную роль, или пустой {@link Optional}, если роль не найдена.
     */
    Optional<Role> findByName(String name);

}
