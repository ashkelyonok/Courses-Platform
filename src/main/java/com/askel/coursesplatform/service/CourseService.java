package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.Course;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;


@Service
public class CourseService {

    private final List<Course> courses = List.of(
            Course.builder().id(1L).name("Business").description("Solve business problems").build(),
            Course.builder().id(2L).name("Marketing").description("Learn in-demand skills").build()
    );

    public List<Course> findAllCourses() {
        return courses;
    }

    public List<Course> getCourseByName(String name) {
        return courses.stream()
                .filter(course -> course.getName().equals(name)).toList();
    }

    public Optional<Course> getCourseById(Long id) {
        return courses.stream()
                .filter(course -> course.getId().equals(id))
                .findFirst();
    }
}
