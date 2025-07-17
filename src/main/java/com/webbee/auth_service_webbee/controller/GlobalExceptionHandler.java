package com.webbee.auth_service_webbee.controller;

import com.webbee.auth_service_webbee.exception.RoleAccessException;
import com.webbee.auth_service_webbee.exception.RoleNotFoundException;
import com.webbee.auth_service_webbee.model.dto.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleRoleException(RoleNotFoundException ex) {
        log.info(ex.getMessage());
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoleAccessException.class)
    public ResponseEntity<ExceptionResponse> handleRoleAccessException(RoleAccessException ex) {
        log.info(ex.getMessage());
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getCode()));
    }

}
