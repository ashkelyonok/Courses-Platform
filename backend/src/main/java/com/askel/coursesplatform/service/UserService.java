package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.dto.UserResponse;
import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.enums.UserRoles;

import java.util.List;

public interface UserService {
//    List<UserResponseDto> getAllUsers();
//
//    UserResponseDto getUserById(Long id);
//
//    UserResponseDto createUser(UserRequestDto userRequestDto);
//
//    UserResponseDto updateUser(Long id, UserRequestDto userRequestDto);
//
//    void deleteUserById(Long id);

    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    List<UserResponse> getUsersByRole(UserRoles role);

    UserResponse createUser(UserRequestDto userRequestDto);
    UserResponse updateUser(Long id, UserRequestDto userRequestDto);
    void deleteUserById(Long id);

    UserResponse getUserByEmail(String email);
    boolean checkEmailExists(String email);
    List<UserResponse> getUsersEnrolledInCourse(Long courseId);
    List<UserResponse> getUsersTeachingCourse(Long courseId);
    List<UserResponse> getUsersByName(String namePart);
    List<UserResponse> getUsersByRoleAndName(UserRoles role, String namePart);
    UserResponse changeUserRole(Long userId, UserRoles newRole);

    List<CourseResponseDto> getCoursesEnrolledByUser(Long userId);
    List<CourseResponseDto> getCoursesTaughtByUser(Long userId);
}
