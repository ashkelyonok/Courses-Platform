package com.askel.coursesplatform.model.dto.response;

import java.util.List;

public record CourseResponseDto(
        Long id,
        String name,
        String description,
        //Long instructorId,
        UserResponseDto instructor,
        List<UserResponseDto> students,
        //List<Long> studentIds,
        String courseStatus
) {}
