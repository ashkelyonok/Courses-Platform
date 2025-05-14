package com.askel.coursesplatform.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequestDto(
        @NotBlank(message = "Course name cannot be blank")
        @Size(max = 255, message = "Course name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Course description cannot be blank")
        @Size(max = 1000, message = "Course description must not exceed 1000 characters")
        String description,

        Long instructorId
) {}
