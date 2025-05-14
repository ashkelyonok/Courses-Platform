package com.askel.coursesplatform.model.dto.response;

import com.askel.coursesplatform.model.dto.UserResponse;
import com.askel.coursesplatform.model.enums.UserRoles;

import java.util.List;

public record InstructorResponseDto(
        Long id,
        String name,
        String email,
        List<Long> taughtCourseIds,
        UserRoles role
) implements UserResponse {}
