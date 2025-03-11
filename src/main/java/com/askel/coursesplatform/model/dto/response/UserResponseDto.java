package com.askel.coursesplatform.model.dto.response;

import java.util.List;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        List<Long> enrolledCourseIds,
        List<Long> taughtCourseIds
) {}
