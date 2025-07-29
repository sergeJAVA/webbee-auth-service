package com.webbee.auth_service_webbee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.auth_service_webbee.model.AuthType;
import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.security.JwtService;
import com.webbee.auth_service_webbee.testcontainer.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class LoginControllerTest extends TestContainer {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        Set<Role> role = roleRepository.findByName("USER").stream().collect(Collectors.toSet());
        testUser = User.builder()
                .username("TestUser")
                .password(passwordEncoder.encode("password123"))
                .email("test@yandex.ru")
                .roles(role)
                .authType(AuthType.LOCAL)
                .build();
        userRepository.save(testUser);
    }

    @Test
    void loginPage_ReturnsLoginHtml() throws Exception {
        mockMvc.perform(get("/loginForm"))
                .andExpect(status().isOk())
                .andExpect(view().name("login.html"));
    }

    @Test
    void successPage_ReturnsMessageWithJWT() throws Exception {
        String token = jwtService.generateJwtToken(testUser);
        mockMvc.perform(get("/success-page").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Success login. Token: " + token));
    }

    @Test
    void successPage_Failure_Because_NoJWTInHeader() throws Exception {
        String token = jwtService.generateJwtToken(testUser);
        mockMvc.perform(get("/success-page"))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.message").value("Authentication required or invalid credentials provided."));
    }

    @Test
    void loginPage_ReturnsFailureMessage() throws Exception {
        mockMvc.perform(get("/failure-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("User with this email address is already registered locally."));
    }

    @Test
    void callback_ReturnsMessageWithJWT() throws Exception {
        String token = jwtService.generateJwtToken(testUser);
        mockMvc.perform(get("/oauth2/callback").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Success login with Google. Token: " + token));
    }

}