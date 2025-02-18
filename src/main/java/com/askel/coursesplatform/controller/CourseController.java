package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.model.Course;
import com.askel.coursesplatform.service.CourseService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/v1/courses")
@AllArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public List<Course> findAllCourses() {
        return courseService.findAllCourses();
    }

    @GetMapping("/search")
    public List<Course> searchCoursesByName(@RequestParam String name) {
        return courseService.getCourseByName(name);
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }
}
