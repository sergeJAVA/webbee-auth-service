package com.webbee.auth_service_webbee.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class RegistrationRequest {

    @NotBlank(message = "The username cannot be blank")
    private String username;

    @NotBlank(message = "The password cannot be blank")
    @Size(min = 8, message = "The password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "The email cannot be blank")
    private String email;

}
