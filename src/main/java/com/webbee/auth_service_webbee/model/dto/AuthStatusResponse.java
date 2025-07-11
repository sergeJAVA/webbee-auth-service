package com.webbee.auth_service_webbee.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class AuthStatusResponse {

    private String state;
    private Integer code;
    private LocalDateTime timestamp;
    private String token;

}
