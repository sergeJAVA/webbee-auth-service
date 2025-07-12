package com.webbee.auth_service_webbee.service.impl;

import com.webbee.auth_service_webbee.exception.RoleNotFoundException;
import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.model.dto.RoleStatusResponse;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();
        adminRole = Role.builder().id(2L).name("ADMIN").build();

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .build();
    }

    @Test
    @DisplayName("Должно успешно изменить роли для существующего пользователя")
    void saveRoles_UserExists_Success() {
        ChangeUserRolesRequest request = new ChangeUserRolesRequest("testuser", new HashSet<>(List.of("ADMIN")));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        RoleStatusResponse response = userRoleService.saveRoles(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("The user's roles have been successfully changed", response.getState());
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getRoles().contains("ADMIN"));
        assertEquals(1, response.getRoles().size());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(roleRepository, times(1)).findByName("ADMIN");
    }

    @Test
    @DisplayName("Должен возвращать BAD_REQUEST, если пользователь не найден при сохранении ролей")
    void saveRoles_UserNotFound() {
        ChangeUserRolesRequest request = new ChangeUserRolesRequest("nonexistent", Collections.singleton("USER"));

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RoleStatusResponse response = userRoleService.saveRoles(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
        assertEquals("The user with the name <<nonexistent>> was not found", response.getState());
        assertNull(response.getRoles());

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбрасывать RoleNotFoundException, если указанная роль не существует")
    void saveRoles_RoleNotFound() {
        ChangeUserRolesRequest request = new ChangeUserRolesRequest("testuser", new LinkedHashSet<>(Arrays.asList("ADMIN", "NON_EXISTENT_ROLE")));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName("NON_EXISTENT_ROLE")).thenReturn(Optional.empty());

        RoleNotFoundException thrown = assertThrows(RoleNotFoundException.class, () -> userRoleService.saveRoles(request));

        assertEquals("There is no role named <<NON_EXISTENT_ROLE>>.", thrown.getMessage());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(roleRepository, times(1)).findByName("ADMIN");
        verify(roleRepository, times(1)).findByName("NON_EXISTENT_ROLE");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пустой список ролей для saveRoles")
    void saveRoles_EmptyRolesList() {
        ChangeUserRolesRequest request = new ChangeUserRolesRequest("testuser", Collections.emptySet());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RoleStatusResponse response = userRoleService.saveRoles(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("The user's roles have been successfully changed", response.getState());
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getRoles().isEmpty());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(roleRepository, never()).findByName(anyString());
    }


    @Test
    @DisplayName("Должен возвращать роли пользователя для существующего пользователя")
    void getRoles_UserExists() {
        testUser.setRoles(new HashSet<>(Arrays.asList(userRole, adminRole)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        List<String> roles = userRoleService.getRoles("testuser");

        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("USER"));
        assertTrue(roles.contains("ADMIN"));

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Должен возвращать пустой список, если пользователь не найден для getRoles")
    void getRoles_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        List<String> roles = userRoleService.getRoles("nonexistent");

        assertNotNull(roles);
        assertTrue(roles.isEmpty());

        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Должен возвращать пустой список, если у пользователя нет ролей")
    void getRoles_UserHasNoRoles() {
        testUser.setRoles(Collections.emptySet());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        List<String> roles = userRoleService.getRoles("testuser");

        assertNotNull(roles);
        assertTrue(roles.isEmpty());

        verify(userRepository, times(1)).findByUsername("testuser");
    }

}