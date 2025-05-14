package com.askel.coursesplatform.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email must not be blank")
        String email,

        @NotBlank(message = "Password must not be blank")
        String password
) {}
