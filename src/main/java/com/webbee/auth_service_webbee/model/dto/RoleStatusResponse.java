package com.webbee.auth_service_webbee.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@Setter
public class RoleStatusResponse {

    private String state;
    private Integer code;
    private LocalDateTime timestamp;
    private String username;
    private Set<String> roles;

}
