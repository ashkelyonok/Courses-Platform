package com.askel.coursesplatform.model.dto.response;

import com.askel.coursesplatform.model.dto.UserResponse;
import java.util.List;

public record StudentResponseDto(
        Long id,
        String name,
        String email,
        List<Long> enrolledCourseIds
) implements UserResponse {}
