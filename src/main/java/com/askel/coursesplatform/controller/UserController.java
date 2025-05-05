package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.model.dto.UserResponse;
import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.enums.UserRoles;
import com.askel.coursesplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "API for managing users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
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

    @PutMapping("/update/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto userRequestDto
    ) {
        UserResponse updatedUser = userService.updateUser(id, userRequestDto);
        return ResponseEntity.ok(updatedUser);
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
}
