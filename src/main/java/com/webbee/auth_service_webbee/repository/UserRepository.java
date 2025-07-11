package com.webbee.auth_service_webbee.repository;

import com.webbee.auth_service_webbee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
