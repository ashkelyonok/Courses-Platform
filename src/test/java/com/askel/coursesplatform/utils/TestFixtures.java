package com.askel.coursesplatform.utils;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.CourseStatus;

import java.util.ArrayList;
import java.util.List;

public class TestFixtures {

    public static Course createCourse(Long id, String name, CourseStatus status, User instructor) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDescription("Description for " + name);
        course.setStatus(status);
        course.setStudents(new ArrayList<>());
        course.setInstructor(instructor);
        return course;
    }

    public static Course createCourse(Long id, String name, CourseStatus status) {
        return createCourse(id, name, status, null);
    }

    public static CourseRequestDto createCourseRequestDto(String name, Long instructorId) {
        return new CourseRequestDto(name, "Description for " + name, instructorId);
    }

    public static CourseResponseDto createCourseResponseDto(Long id, String name, CourseStatus status, UserResponseDto instructor) {
        return new CourseResponseDto(
                id, name, "Description for " + name, instructor, new ArrayList<>(), status.name());
    }

    public static CourseResponseDto createCourseResponseDto(Long id, String name, CourseStatus status) {
        return createCourseResponseDto(id, name, status, null);
    }

    // User fixtures
    public static User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setTaughtCourses(new ArrayList<>());
        user.setEnrolledCourses(new ArrayList<>());
        return user;
    }

    public static UserRequestDto createUserRequestDto(String name, String email) {
        return new UserRequestDto(name, email);
    }

    public static UserResponseDto createUserResponseDto(Long id, String name, String email) {
        return new UserResponseDto(id, name, email, new ArrayList<>(), new ArrayList<>());
    }

    public static UserResponseDto createUserResponseDto(Long id, String name, String email, List<Long> enrolledCourseIds, List<Long> taughtCourseIds) {
        return new UserResponseDto(id, name, email, enrolledCourseIds, taughtCourseIds);
    }
}
