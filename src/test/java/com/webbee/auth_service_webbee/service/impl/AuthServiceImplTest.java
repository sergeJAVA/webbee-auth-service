package com.webbee.auth_service_webbee.service.impl;

import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.dto.AuthStatusResponse;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;
import com.webbee.auth_service_webbee.model.security.CustomUserDetails;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationProvider authenticationProvider;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .roles(Collections.singleton(userRole))
                .build();
    }

    @Test
    @DisplayName("Должен успешно зарегистрировать нового пользователя")
    void registration_Success() {
        RegistrationRequest request = new RegistrationRequest("newuser", "password123", "newuser@gmail.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthStatusResponse response = authService.registration(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("User has been successfully registered", response.getState());
        assertNotNull(response.getTimestamp());

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(roleRepository, times(1)).findByName("USER");
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Должен возвращать BAD_REQUEST, если email недействителен")
    void registration_InvalidEmail() {
        RegistrationRequest request = new RegistrationRequest("newuser", "password123", "invalid-email");

        AuthStatusResponse response = authService.registration(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
        assertEquals("The email <<invalid-email>> is not valid!", response.getState());

        verify(userRepository, never()).findByUsername(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("Должен возвращать BAD_REQUEST, если имя пользователя уже существует")
    void registration_UsernameAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest("testuser", "password123", "newemail@gmail.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));

        AuthStatusResponse response = authService.registration(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
        assertEquals("The user with this username already exist", response.getState());

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен возвращать BAD_REQUEST, если электронная почта уже существует")
    void registration_EmailAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest("anotheruser", "password123", "test@gmail.com");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));

        AuthStatusResponse response = authService.registration(request);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
        assertEquals("The user with such email <<test@gmail.com>> already exists", response.getState());

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Должен успешно войти в систему пользователя и вернуть токен")
    void login_Success() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        CustomUserDetails userDetails = new CustomUserDetails(
                testUser.getUsername(),
                testUser.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                testUser.getEmail()
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateJwtToken(userDetails, testUser.getId())).thenReturn("jwt_token");

        AuthStatusResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("User has been authorized", response.getState());
        assertEquals("jwt_token", response.getToken());
        assertNotNull(response.getTimestamp());

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(authenticationProvider, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateJwtToken(userDetails, testUser.getId());
    }

    @Test
    @DisplayName("Должен возвращать FORBIDDEN для неправильных учетных данных при входе в систему")
    void login_IncorrectCredentials() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(testUser));
        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthStatusResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getCode());
        assertEquals("Incorrect username or password is specified", response.getState());
        assertNull(response.getToken());
        assertNotNull(response.getTimestamp());

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(authenticationProvider, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateJwtToken(any(CustomUserDetails.class), anyLong());
    }

    @Test
    @DisplayName("Должно возвращать FORBIDDEN, если пользователь не найден при входе в систему")
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest("nonexistentuser", "password123");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(authenticationProvider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthStatusResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getCode());
        assertEquals("Incorrect username or password is specified", response.getState());
        assertNull(response.getToken());
        assertNotNull(response.getTimestamp());

        verify(userRepository, times(1)).findByUsername(request.getUsername());
        verify(authenticationProvider, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateJwtToken(any(CustomUserDetails.class), anyLong());
    }

}