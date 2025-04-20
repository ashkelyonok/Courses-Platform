package com.askel.coursesplatform.service;

import com.askel.coursesplatform.cache.UserCache;
import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.CourseStatus;
import com.askel.coursesplatform.repository.CourseRepository;
import com.askel.coursesplatform.repository.UserRepository;
import com.askel.coursesplatform.service.impl.UserServiceImpl;
import com.askel.coursesplatform.service.mapper.UserMapper;
import com.askel.coursesplatform.utils.ErrorMessages;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.askel.coursesplatform.utils.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserCache userCache;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private Course course;

    @BeforeEach
    void setUp() {
        user = createUser(1L, "John Doe", "john@example.com");
        userRequestDto = createUserRequestDto("John Doe", "john@example.com");
        userResponseDto = createUserResponseDto(1L, "John Doe", "john@example.com");
        course = createCourse(1L, "Java Course", CourseStatus.ACTIVE);
    }

    @Test
    void getAllUsers_returnsUsers() {
        // Arrange
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        // Act
        List<UserResponseDto> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(userResponseDto, result.getFirst());
        assertNotNull(result.getFirst().enrolledCourseIds());
        assertNotNull(result.getFirst().taughtCourseIds());
        verify(userRepository).findAll();
        verify(userMapper).toUserResponseDto(user);
        verifyNoMoreInteractions(userRepository, userMapper);
        verifyNoInteractions(courseRepository, userCache);
    }

    @Test
    void getAllUsers_emptyList_returnsEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserResponseDto> result = userService.getAllUsers();

        // Assert
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper, courseRepository, userCache);
    }

    @Test
    void getUserById_withValidId_returnsUser() {
        // Arrange
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = userService.getUserById(1L);

        // Assert
        assertEquals(userResponseDto, result);
        assertNotNull(result.enrolledCourseIds());
        assertNotNull(result.taughtCourseIds());
        verify(userCache).get(1L);
        verify(userRepository).findById(1L);
        verify(userCache).put(1L, user);
        verify(userMapper).toUserResponseDto(user);
        verifyNoMoreInteractions(userCache, userRepository, userMapper);
        verifyNoInteractions(courseRepository);
    }

    @Test
    void getUserById_withCachedUser_returnsUser() {
        // Arrange
        when(userCache.get(1L)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = userService.getUserById(1L);

        // Assert
        assertEquals(userResponseDto, result);
        assertNotNull(result.enrolledCourseIds());
        assertNotNull(result.taughtCourseIds());
        verify(userCache).get(1L);
        verify(userMapper).toUserResponseDto(user);
        verifyNoMoreInteractions(userCache, userMapper);
        verifyNoInteractions(userRepository, courseRepository);
    }

    @Test
    void getUserById_notFound_throwsEntityNotFoundException() {
        // Arrange
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(1L)
        );
        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userCache).get(1L);
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userCache, userRepository);
        verifyNoInteractions(userMapper, courseRepository);
    }

    @Test
    void createUser_withValidData_returnsCreatedUser() {
        // Arrange
        when(userMapper.toUser(userRequestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponseDto(user)).thenReturn(userResponseDto);

        // Act
        UserResponseDto result = userService.createUser(userRequestDto);

        // Assert
        assertEquals(userResponseDto, result);
        assertNotNull(result.enrolledCourseIds());
        assertNotNull(result.taughtCourseIds());
        verify(userMapper).toUser(userRequestDto);
        verify(userRepository).save(user);
        verify(userCache).put(1L, user);
        verify(userMapper).toUserResponseDto(user);
        verifyNoMoreInteractions(userRepository, userMapper, userCache);
        verifyNoInteractions(courseRepository);
    }

    @Test
    void updateUser_withValidData_returnsUpdatedUser() {
        // Arrange
        UserRequestDto updateDto = createUserRequestDto("Jane Doe", "jane@example.com");
        User updatedUser = createUser(1L, "Jane Doe", "jane@example.com");
        UserResponseDto updatedResponseDto = createUserResponseDto(1L, "Jane Doe", "jane@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(updatedUser);
        when(userMapper.toUserResponseDto(updatedUser)).thenReturn(updatedResponseDto);

        // Act
        UserResponseDto result = userService.updateUser(1L, updateDto);

        // Assert
        assertEquals(updatedResponseDto, result);
        assertNotNull(result.enrolledCourseIds());
        assertNotNull(result.taughtCourseIds());
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(userCache).put(1L, updatedUser);
        verify(userMapper).toUserResponseDto(updatedUser);
        verifyNoMoreInteractions(userRepository, userMapper, userCache);
        verifyNoInteractions(courseRepository);
    }

    @Test
    void updateUser_notFound_throwsEntityNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(1L, userRequestDto)
        );
        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper, courseRepository, userCache);
    }

    @Test
    void deleteUserById_withEnrolledAndTaughtCourses_deletesUser() {
        // Arrange
        user.getEnrolledCourses().add(course);
        user.getTaughtCourses().add(course);
        course.getStudents().add(user);
        course.setInstructor(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.save(course)).thenReturn(course);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUserById(1L);

        // Assert
        assertFalse(course.getStudents().contains(user));
        assertNull(course.getInstructor());
        assertEquals(CourseStatus.PENDING_INSTRUCTOR, course.getStatus());
        verify(userRepository).findById(1L);
        verify(courseRepository).save(course);
        verify(userRepository).deleteById(1L);
        verify(userCache).remove(1L);
        verifyNoMoreInteractions(userRepository, courseRepository, userCache);
        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteUserById_notFound_throwsEntityNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUserById(1L)
        );
        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(courseRepository, userMapper, userCache);
    }
}
