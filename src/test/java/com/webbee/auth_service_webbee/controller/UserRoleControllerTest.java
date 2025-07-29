package com.webbee.auth_service_webbee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webbee.auth_service_webbee.model.AuthType;
import com.webbee.auth_service_webbee.model.Role;
import com.webbee.auth_service_webbee.model.User;
import com.webbee.auth_service_webbee.model.dto.ChangeUserRolesRequest;
import com.webbee.auth_service_webbee.repository.RoleRepository;
import com.webbee.auth_service_webbee.repository.UserRepository;
import com.webbee.auth_service_webbee.service.UserRoleService;
import com.webbee.auth_service_webbee.service.security.JwtService;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserRoleControllerTest extends TestContainer {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRoleService userRoleService;
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
    private User testAdmin;

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        Set<Role> adminRole = roleRepository.findByName("ADMIN").stream().collect(Collectors.toSet());
        Set<Role> userRole = roleRepository.findByName("USER").stream().collect(Collectors.toSet());

        testUser = User.builder()
                .username("TestUser")
                .password(passwordEncoder.encode("password123"))
                .email("test@yandex.ru")
                .roles(userRole)
                .authType(AuthType.LOCAL)
                .build();

        testAdmin = User.builder()
                .username("Admin")
                .password(passwordEncoder.encode("password123"))
                .email("testadmin@yandex.ru")
                .roles(adminRole)
                .authType(AuthType.LOCAL)
                .build();

        userRepository.saveAll(List.of(testUser, testAdmin));
    }

    @Test
    void save_Success_UserHasAdminRole_And_UserExist() throws Exception{
        ChangeUserRolesRequest request = ChangeUserRolesRequest.builder()
                .username("TestUser")
                .roles(Set.of("CONTRACTOR_RUS", "USER"))
                .build();
        mockMvc.perform(put("/user-roles/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + jwtService.generateJwtToken(testAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("The user's roles have been successfully changed"))
                .andExpect(jsonPath("$.roles", hasItems("CONTRACTOR_RUS", "USER")));
    }

    @Test
    void save_Failure_UserHasAdminRole_But_UserNotExist() throws Exception{
        ChangeUserRolesRequest request = ChangeUserRolesRequest.builder()
                .username("WrongUser")
                .roles(Set.of("CONTRACTOR_RUS", "USER"))
                .build();
        mockMvc.perform(put("/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + jwtService.generateJwtToken(testAdmin)))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.state").value("The user with the name <<" + request.getUsername() + ">> was not found"));
    }

    @Test
    void save_Failure_UserHasNoAdminRole() throws Exception{
        ChangeUserRolesRequest request = ChangeUserRolesRequest.builder()
                .username("TestUser")
                .roles(Set.of("CONTRACTOR_RUS", "USER"))
                .build();
        mockMvc.perform(put("/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + jwtService.generateJwtToken(testUser)))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.message").value("You do not have sufficient permissions to access this resource. Required role: ADMIN."))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void getUserRoles_Success_And_HasAdminRoleToGetRolesAnotherUser() throws Exception{
        mockMvc.perform(get("/user-roles/TestUser")
                .header("Authorization", "Bearer " + jwtService.generateJwtToken(testAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("USER")));
    }

    @Test
    void getUserRoles_Success_And_UserHasNoRoleAdmin() throws Exception{
        mockMvc.perform(get("/user-roles/" + testUser.getUsername())
                        .header("Authorization", "Bearer " + jwtService.generateJwtToken(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("USER")));
    }

    @Test
    void getUserRoles_Failure_Because_UserHasNoRoleAdmin() throws Exception{
        mockMvc.perform(get("/user-roles/Admin")
                        .header("Authorization", "Bearer " + jwtService.generateJwtToken(testUser)))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.message").value("Without an admin role, you can only view your roles!"));
    }

    @Test
    void getUserRoles_Failure_Because_NoAuthentication() throws Exception{
        mockMvc.perform(get("/user-roles/Admin"))
                .andExpect(status().is(401));
    }

}