package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.service.CourseService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        List<CourseResponseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable Long id) {
        CourseResponseDto courseResponseDto = courseService.getCourseById(id);
        return ResponseEntity.ok(courseResponseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseResponseDto>> searchCoursesByName(@RequestParam String name) {
        List<CourseResponseDto> courses = courseService.getCourseByName(name);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByStudentId(
            @PathVariable Long studentId) {
        List<CourseResponseDto> courses = courseService.getCoursesByStudentId(studentId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/name")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByStudentName(
            @RequestParam String studentName) {
        List<CourseResponseDto> courses = courseService.getCoursesByStudentName(studentName);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByInstructorId(
            @PathVariable Long instructorId) {
        List<CourseResponseDto> courses = courseService.getCoursesByInstructorId(instructorId);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/create")
    public ResponseEntity<CourseResponseDto> createCourse(
            @RequestBody CourseRequestDto courseRequestDto
    ) {
        CourseResponseDto createdCourse = courseService.createCourse(courseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseRequestDto courseRequestDto
    ) {
        CourseResponseDto updatedCourse = courseService.updateCourse(id, courseRequestDto);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourseById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/student/{studentId}")
    public ResponseEntity<Void> addStudentToCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        courseService.addStudentToCourse(courseId, studentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/student/{studentId}")
    public ResponseEntity<Void> removeStudentFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long studentId) {
        courseService.removeStudentFromCourse(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{courseId}/instructor/{instructorId}")
    public ResponseEntity<CourseResponseDto> assignInstructorToCourse(
            @PathVariable Long courseId,
            @PathVariable Long instructorId) {
        CourseResponseDto updatedCourse
                = courseService.assignInstructorToCourse(courseId, instructorId);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}/instructor/{instructorId}")
    public ResponseEntity<Void> unassignInstructorFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long instructorId) {
        courseService.unassignInstructorFromCourse(courseId, instructorId);
        return ResponseEntity.noContent().build();
    }
}
