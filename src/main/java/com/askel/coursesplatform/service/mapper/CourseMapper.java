package com.askel.coursesplatform.service.mapper;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CourseMapper {

    public CourseResponseDto toCourseResponseDto(Course course) {
        return new CourseResponseDto(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getInstructor() != null ? course.getInstructor().getId() : null,
                course.getStudents().stream()
                        .map(User::getId)
                        .toList(),
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
