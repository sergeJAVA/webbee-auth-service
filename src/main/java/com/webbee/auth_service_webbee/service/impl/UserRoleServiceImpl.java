package com.webbee.auth_service_webbee.service.impl;

import com.webbee.auth_service_webbee.exception.RoleNotFoundException;
import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;
import org.springframework.transaction.annotation.Transactional;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleStatusResponse saveRoles(ChangeUserRolesRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());

        RoleStatusResponse response;

        if (user.isPresent()) {
            Set<Role> roles = new HashSet<>();
            request.getRoles().forEach(role -> {
                Role rl = roleRepository.findByName(role)
                        .orElseThrow(() -> new RoleNotFoundException("There is no role named <<" + role + ">>."));

                roles.add(rl);
            });

            user.get().setRoles(roles);

            Set<String> strRoles = roles.stream().map(Role::getName).collect(Collectors.toSet());

            response = RoleStatusResponse.builder()
                    .code(200)
                    .timestamp(LocalDateTime.now())
                    .state("The user's roles have been successfully changed")
                    .username(request.getUsername())
                    .roles(strRoles)
                    .build();

        } else {

            response = RoleStatusResponse.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .timestamp(LocalDateTime.now())
                    .state("The user with the name <<" + request.getUsername() + ">> was not found")
                    .username(request.getUsername())
                    .roles(null)
                    .build();

        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoles(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

}
