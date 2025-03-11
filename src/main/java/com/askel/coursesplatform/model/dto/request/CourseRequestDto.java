package com.askel.coursesplatform.model.dto.request;

public record CourseRequestDto(
        String name,
        String description,
        Long instructorId
) {}
