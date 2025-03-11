package com.askel.coursesplatform.model.dto.response;

import java.util.List;

public record CourseResponseDto(
        Long id,
        String name,
        String description,
        Long instructorId,
        List<Long> studentIds,
        String courseStatus
) {}
