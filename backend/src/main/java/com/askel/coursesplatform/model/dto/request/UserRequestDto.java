package com.askel.coursesplatform.model.dto.request;

import com.askel.coursesplatform.model.enums.UserRoles;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "User name cannot be blank")
        @Size(max = 127, message = "User username not exceed 127 characters")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 5, message = "Password must be at least 5 characters")
        String password,

        UserRoles role
) {}
