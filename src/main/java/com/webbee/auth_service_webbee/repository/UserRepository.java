package com.webbee.auth_service_webbee.repository;

import com.webbee.auth_service_webbee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * {@code UserRepository} — это интерфейс репозитория Spring Data JPA для сущности {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит сущность {@link User} по ее имени пользователя.
     *
     * @param username Имя пользователя для поиска.
     * @return {@link Optional}, содержащий найденного пользователя, или пустой {@link Optional}, если пользователь не найден.
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит сущность {@link User} по ее адресу электронной почты.
     *
     * @param email Адрес электронной почты для поиска.
     * @return {@link Optional}, содержащий найденного пользователя, или пустой {@link Optional}, если пользователь не найден.
     */
    Optional<User> findByEmail(String email);

}
