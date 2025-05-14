package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.enums.CourseStatus;
import com.askel.coursesplatform.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@Tag(name = "Course API", description = "API for managing courses")
public class CourseController {

    private final CourseService courseService;
    //add valid, requestBody, change responseEntity params

    @GetMapping
    @Operation(summary = "Get all courses")
    public ResponseEntity<List<CourseResponseDto>> getAllCourses() {
        List<CourseResponseDto> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<CourseResponseDto> getCourseById(@PathVariable Long id) {
        CourseResponseDto courseResponseDto = courseService.getCourseById(id);
        return ResponseEntity.ok(courseResponseDto);
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses by name")
    public ResponseEntity<List<CourseResponseDto>> searchCoursesByName(@RequestParam String name) {
        List<CourseResponseDto> courses = courseService.getCourseByName(name);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get courses by student ID")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByStudentId(
            @PathVariable Long studentId) {
        List<CourseResponseDto> courses = courseService.getCoursesByStudentId(studentId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/name")
    @Operation(summary = "Get courses by student name")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByStudentName(
            @RequestParam String studentName) {
        List<CourseResponseDto> courses = courseService.getCoursesByStudentName(studentName);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(summary = "Get courses by instructor ID")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByInstructorId(
            @PathVariable Long instructorId) {
        List<CourseResponseDto> courses = courseService.getCoursesByInstructorId(instructorId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/instructor/name")
    @Operation(summary = "Get courses by instructor name")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByInstructorName(
            @RequestParam String instructorName) {
        List<CourseResponseDto> courses = courseService.getCoursesByInstructorName(instructorName);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/create")
    //@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @Operation(summary = "Create a new course")
    public ResponseEntity<CourseResponseDto> createCourse(
            @Valid @RequestBody CourseRequestDto courseRequestDto
    ) {
        CourseResponseDto createdCourse = courseService.createCourse(courseRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @Validated
    @PostMapping("/bulk")
    @Operation(summary = "Create courses")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseResponseDto>> createCoursesBulk(
            @RequestBody List<CourseRequestDto> courseRequestDtos
    ) {
        List<CourseResponseDto> createCourses = courseService.createCourses(courseRequestDtos);
        return ResponseEntity.ok(createCourses);
    }

    @PutMapping("/update/{id}")
    //@PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @securityService.isCourseInstructor(#id))")
    @Operation(summary = "Update course")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequestDto courseRequestDto
    ) {
        CourseResponseDto updatedCourse = courseService.updateCourse(id, courseRequestDto);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete course by ID")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourseById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/student/{studentId}")
    @Operation(summary = "Add student to course")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @securityService.isCourseInstructor(#courseId))")
    public ResponseEntity<Void> addStudentToCourse(
            @PathVariable @Parameter(description = "ID of the course") Long courseId,
            @PathVariable @Parameter(description = "ID of the student to add") Long studentId) {
        courseService.addStudentToCourse(courseId, studentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{courseId}/student/{studentId}")
    @Operation(summary = "Remove student from course")
    //@PreAuthorize("hasRole('ADMIN') or (hasRole('INSTRUCTOR') and @securityService.isCourseInstructor(#courseId))")
    public ResponseEntity<Void> removeStudentFromCourse(
            @PathVariable @Parameter(description = "ID of the course") Long courseId,
            @PathVariable @Parameter(description = "ID of the student to remove") Long studentId) {
        courseService.removeStudentFromCourse(courseId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{courseId}/instructor/{instructorId}")
    @Operation(summary = "Assign instructor to course")
    public ResponseEntity<CourseResponseDto> assignInstructorToCourse(
            @PathVariable Long courseId,
            @PathVariable Long instructorId) {
        CourseResponseDto updatedCourse
                = courseService.assignInstructorToCourse(courseId, instructorId);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{courseId}/instructor/{instructorId}")
    @Operation(summary = "Unassign instructor from course")
    public ResponseEntity<Void> unassignInstructorFromCourse(
            @PathVariable Long courseId,
            @PathVariable Long instructorId) {
        courseService.unassignInstructorFromCourse(courseId, instructorId);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/status/{status}")
    @Operation(summary = "Get courses by status")
    public ResponseEntity<List<CourseResponseDto>> getCoursesByStatus(
            @PathVariable CourseStatus status) {
        return ResponseEntity.ok(courseService.getCoursesByStatus(status));
    }

    @GetMapping("/search/description")
    @Operation(summary = "Search courses by description")
    public ResponseEntity<List<CourseResponseDto>> searchCoursesByDescription(
            @RequestParam String description) {
        return ResponseEntity.ok(courseService.getCoursesByDescription(description));
    }

    @GetMapping("/search/status")
    @Operation(summary = "Search courses by status and name")
    public ResponseEntity<List<CourseResponseDto>> searchCoursesByStatusAndName(
            @RequestParam CourseStatus status,
            @RequestParam String name) {
        return ResponseEntity.ok(courseService.getCoursesByStatusAndName(status, name));
    }

    @GetMapping("/without-students")
    @Operation(summary = "Get courses without students")
    public ResponseEntity<List<CourseResponseDto>> getCoursesWithoutStudents() {
        return ResponseEntity.ok(courseService.getCoursesWithoutStudents());
    }

    @GetMapping("/without-instructor")
    @Operation(summary = "Get courses without instructor")
    public ResponseEntity<List<CourseResponseDto>> getCoursesWithoutInstructor() {
        return ResponseEntity.ok(courseService.getCoursesWithoutInstructor());
    }

    @GetMapping("/with-instructor")
    @Operation(summary = "Get courses with instructor")
    public ResponseEntity<List<CourseResponseDto>> getCoursesWithInstructor() {
        return ResponseEntity.ok(courseService.getCoursesWithInstructor());
    }

    @GetMapping("/with-students")
    @Operation(summary = "Get courses with students")
    public ResponseEntity<List<CourseResponseDto>> getCoursesWithStudents() {
        return ResponseEntity.ok(courseService.getCoursesWithStudents());
    }
}
