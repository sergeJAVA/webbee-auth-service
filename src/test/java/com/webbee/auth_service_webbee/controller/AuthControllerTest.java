package com.webbee.auth_service_webbee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.auth_service_webbee.model.AuthType;
import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.dto.LoginRequest;
import com.webbee.auth_service_webbee.model.dto.RegistrationRequest;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.AuthService;
import com.webbee.auth_service_webbee.testcontainer.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AuthControllerTest extends TestContainer{

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        Set<Role> role = roleRepository.findByName("USER").stream().collect(Collectors.toSet());
        User testUser = User.builder()
                .username("TestUser")
                .password(passwordEncoder.encode("password123"))
                .email("test@yandex.ru")
                .roles(role)
                .authType(AuthType.LOCAL)
                .build();
        userRepository.save(testUser);
    }

    @Test
    void signIn_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("TestUser", "password123");
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("User has been authorized"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void signIn_Failure_WrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("TestUser", "wrongPassword");
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.state").value("Incorrect username or password is specified"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void signIn_Failure_WrongUsername() throws Exception {
        LoginRequest loginRequest = new LoginRequest("WrongUser", "password123");
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.state").value("Incorrect username or password is specified"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void signUp_Success() throws Exception {
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .username("Serega")
                .password("password123")
                .email("serega@gmail.com")
                .build();
        mockMvc.perform(put("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("User has been successfully registered"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void signUp_Failure_NotValidEmail() throws Exception {
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .username("Serega")
                .password("password123")
                .email("wrongEmail")
                .build();
        mockMvc.perform(put("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.state").value("The email <<" + regRequest.getEmail() + ">> is not valid!"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void signUp_Failure_UserExist() throws Exception {
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .username("TestUser")
                .password("password123")
                .email("serega@gmail.com")
                .build();
        mockMvc.perform(put("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.state").value("The user with this username already exist"));
    }

    @Test
    void signUp_Failure_EmailExist() throws Exception {
        RegistrationRequest regRequest = RegistrationRequest.builder()
                .username("Serega")
                .password("password123")
                .email("test@yandex.ru")
                .build();
        mockMvc.perform(put("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.state").value("The user with such email <<" + regRequest.getEmail() + ">> already exists"));
    }

}