package com.askel.coursesplatform.factory;

import com.askel.coursesplatform.model.dto.UserResponse;
import com.askel.coursesplatform.model.dto.response.InstructorResponseDto;
import com.askel.coursesplatform.model.dto.response.StudentResponseDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDtoFactory {

    private final CourseRepository courseRepository;

    public UserResponse create(User user) {
        return switch (user.getRole()) {
            case STUDENT -> new StudentResponseDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    mapCoursesToIds(user.getEnrolledCourses())
            );
            case INSTRUCTOR -> new InstructorResponseDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    mapCoursesToIds(user.getTaughtCourses())
            );
            case ADMIN -> new UserResponseDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    mapCoursesToIds(user.getEnrolledCourses()),
                    mapCoursesToIds(user.getTaughtCourses())
            );
            case USER -> new UserResponseDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    mapCoursesToIds(user.getEnrolledCourses()),
                    mapCoursesToIds(user.getTaughtCourses())
            );
        };
    }

    private List<Long> mapCoursesToIds(List<Course> courses) {
        return courses.stream()
                .map(Course::getId)
                .toList();
    }
}
