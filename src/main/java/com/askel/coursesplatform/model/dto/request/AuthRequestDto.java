package com.askel.coursesplatform.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDto(
        @NotBlank(message = "Name must not be blank")
        @Size(max = 127, message = "User name must not exceed 127 characters")
        String name,

        @Email(message = "Email must be valid")
        @NotBlank(message = "Email must not be blank")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {}
