package com.askel.coursesplatform.service.mapper;

import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toUser(UserRequestDto dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        return user;
    }

    public UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getEnrolledCourses().stream()
                        .map(Course::getId)
                        .toList(),
                user.getTaughtCourses().stream()
                        .map(Course::getId)
                        .toList()
        );
    }
}
