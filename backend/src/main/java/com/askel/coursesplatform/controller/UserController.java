package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.model.ErrorResponse;
import com.askel.coursesplatform.model.dto.UserResponse;
import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.UserRoles;
import com.askel.coursesplatform.security.JwtService;
import com.askel.coursesplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "API for managing users")
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

//    @GetMapping("/profile")
//    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication required"));
//        }
//        // Assuming UserService can find user by email
//        return ResponseEntity.ok(userService.getUserByEmail(userDetails.getUsername()));
//    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestHeader("Authorization") String authHeader) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication required"));
        }

        try {
            // Extract JWT from Authorization header (Bearer <token>)
            String jwt = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            Long userId = jwtService.extractUserId(jwt);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid token: missing user ID"));
            }

            UserResponse userResponse = userService.getUserById(userId);
            return ResponseEntity.ok(userResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Failed to load user profile: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponse createdUser = userService.createUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

//    @PutMapping("/update/{id}")
//    @Operation(summary = "Update user")
//    public ResponseEntity<UserResponse> updateUser(
//            @PathVariable Long id,
//            @Valid @RequestBody UserRequestDto userRequestDto
//    ) {
//        UserResponse updatedUser = userService.updateUser(id, userRequestDto);
//        return ResponseEntity.ok(updatedUser);
//    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UserRequestDto userRequest,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        @RequestHeader("Authorization") String authHeader) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication required"));
        }

        try {
            // Verify the user is updating their own profile
            String jwt = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            Long userId = jwtService.extractUserId(jwt);
            if (userId == null || !userId.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You can only update your own profile"));
            }

            UserResponse response = userService.updateUser(id, userRequest);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }




    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check if email exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        return ResponseEntity.ok(userService.checkEmailExists(email));
    }

    @GetMapping("/course/{courseId}/students")
    @Operation(summary = "Get students enrolled in course")
    public ResponseEntity<List<UserResponse>> getCourseStudents(@PathVariable Long courseId) {
        return ResponseEntity.ok(userService.getUsersEnrolledInCourse(courseId));
    }

    @GetMapping("/course/{courseId}/instructors")
    @Operation(summary = "Get instructors teaching course")
    public ResponseEntity<List<UserResponse>> getCourseInstructors(@PathVariable Long courseId) {
        return ResponseEntity.ok(userService.getUsersTeachingCourse(courseId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name")
    public ResponseEntity<List<UserResponse>> searchUsersByName(@RequestParam String name) {
        return ResponseEntity.ok(userService.getUsersByName(name));
    }

    @GetMapping("/search-by-role")
    @Operation(summary = "Search users by role and name")
    public ResponseEntity<List<UserResponse>> searchUsersByRoleAndName(
            @RequestParam UserRoles role,
            @RequestParam String name) {
        return ResponseEntity.ok(userService.getUsersByRoleAndName(role, name));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Change user role")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable Long userId,
            @RequestParam UserRoles newRole) {
        return ResponseEntity.ok(userService.changeUserRole(userId, newRole));
    }

    @GetMapping("/{userId}/enrolled-courses")
    @Operation(summary = "Get all courses where user is enrolled as student")
    public ResponseEntity<List<CourseResponseDto>> getEnrolledCourses(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getCoursesEnrolledByUser(userId));
    }

    @GetMapping("/{userId}/taught-courses")
    @Operation(summary = "Get all courses taught by user as instructor")
    public ResponseEntity<List<CourseResponseDto>> getTaughtCourses(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getCoursesTaughtByUser(userId));
    }
}
