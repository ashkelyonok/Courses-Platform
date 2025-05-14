package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.enums.CourseStatus;

import java.util.List;


public interface CourseService {
    List<CourseResponseDto> getAllCourses();

    CourseResponseDto getCourseById(Long id);

    List<CourseResponseDto> getCourseByName(String name);

    List<CourseResponseDto> getCoursesByStudentId(Long studentId);

    List<CourseResponseDto> getCoursesByStudentName(String studentName);

    List<CourseResponseDto> getCoursesByInstructorId(Long instructorId);

    List<CourseResponseDto> getCoursesByInstructorName(String instructorName);

    CourseResponseDto createCourse(CourseRequestDto courseRequestDto);

    List<CourseResponseDto> createCourses(List<CourseRequestDto> courseRequestDtos);

    CourseResponseDto updateCourse(Long id, CourseRequestDto courseRequestDto);

    void deleteCourseById(Long id);

    void addStudentToCourse(Long courseId, Long studentId);

    void removeStudentFromCourse(Long courseId, Long studentId);

    CourseResponseDto assignInstructorToCourse(Long courseId, Long instructorId);

    void unassignInstructorFromCourse(Long courseId, Long instructorId);



    List<CourseResponseDto> getCoursesByStatus(CourseStatus status);

    List<CourseResponseDto> getCoursesByDescription(String descriptionPart);

    List<CourseResponseDto> getCoursesByStatusAndName(CourseStatus status, String namePart);

    List<CourseResponseDto> getCoursesWithoutStudents();

    List<CourseResponseDto> getCoursesWithoutInstructor();

    List<CourseResponseDto> getCoursesWithInstructor();

    List<CourseResponseDto> getCoursesWithStudents();
}
