package com.askel.coursesplatform.model.dto.response;

import com.askel.coursesplatform.model.dto.UserResponse;

import java.util.List;

public record CourseResponseDto(
        Long id,
        String name,
        String description,
        //Long instructorId,
//        UserResponseDto instructor,
//        List<UserResponseDto> students,

        UserResponse instructor,
        List<UserResponse> students,
        //List<Long> studentIds,
        String courseStatus
) {}
