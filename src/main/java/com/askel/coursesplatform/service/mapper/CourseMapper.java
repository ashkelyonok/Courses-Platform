package com.askel.coursesplatform.service.mapper;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final UserMapper userMapper;

    public CourseResponseDto toCourseResponseDto(Course course) {
        UserResponseDto instructorDto = course.getInstructor() != null
                ? userMapper.toUserResponseDto(course.getInstructor())
                : null;

        List<UserResponseDto> studentDtos = course.getStudents().stream()
                .map(userMapper::toUserResponseDto) // Преобразуем студентов в DTO
                .toList();

        return new CourseResponseDto(
                course.getId(),
                course.getName(),
                course.getDescription(),
                instructorDto, // Передаем DTO инструктора
                studentDtos,   // Передаем список DTO студентов
                course.getStatus().name()
        );
    }

    public Course toCourse(CourseRequestDto dto, User instructor) {
        Course course = new Course();
        course.setName(dto.name());
        course.setDescription(dto.description());
        course.setInstructor(instructor);
        course.setStudents(new ArrayList<>());
        return course;
    }
}
