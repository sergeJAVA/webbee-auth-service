package com.webbee.auth_service_webbee.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class RoleAccessException extends RuntimeException {

    @Getter
    private String message;

    @Getter
    private HttpStatus status;

    public RoleAccessException(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}
