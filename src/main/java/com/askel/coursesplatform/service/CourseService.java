package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import java.util.List;


public interface CourseService {
    List<CourseResponseDto> getAllCourses();

    CourseResponseDto getCourseById(Long id);

    List<CourseResponseDto> getCourseByName(String name);

    List<CourseResponseDto> getCoursesByStudentId(Long studentId);

    List<CourseResponseDto> getCoursesByInstructorId(Long instructorId);

    CourseResponseDto createCourse(CourseRequestDto courseRequestDto);

    CourseResponseDto updateCourse(Long id, CourseRequestDto courseRequestDto);

    void deleteCourseById(Long id);

    void addStudentToCourse(Long courseId, Long studentId);

    void removeStudentFromCourse(Long courseId, Long studentId);

    CourseResponseDto assignInstructorToCourse(Long courseId, Long instructorId);
}
