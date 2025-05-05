package com.askel.coursesplatform.service.mapper;

import com.askel.coursesplatform.factory.UserDtoFactory;
import com.askel.coursesplatform.model.dto.UserResponse;
import com.askel.coursesplatform.model.dto.request.AuthRequestDto;
import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.UserRoles;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final UserDtoFactory dtoFactory;

    public UserMapper(UserDtoFactory dtoFactory) {
        this.dtoFactory = dtoFactory;
    }

//    public User toUser(UserRequestDto dto) {
//        User user = new User();
//        user.setName(dto.name());
//        user.setEmail(dto.email());
//        return user;
//    }
//
//    public UserResponseDto toUserResponseDto(User user) {
//        return new UserResponseDto(
//                user.getId(),
//                user.getName(),
//                user.getEmail(),
//                user.getEnrolledCourses().stream()
//                        .map(Course::getId)
//                        .toList(),
//                user.getTaughtCourses().stream()
//                        .map(Course::getId)
//                        .toList()
//        );
//    }

    public UserResponse toResponseDto(User user) {
        return dtoFactory.create(user);
    }

    public User toUserFromDto(AuthRequestDto dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        return user;
    }

    public User toUserFromDto(UserRequestDto dto) {
        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setRole(dto.role());
        return user;
    }
}
