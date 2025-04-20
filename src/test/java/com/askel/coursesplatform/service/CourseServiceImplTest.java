package com.askel.coursesplatform.service;

import com.askel.coursesplatform.cache.CourseCache;
import com.askel.coursesplatform.exception.CourseAlreadyExistsException;
import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.CourseStatus;
import com.askel.coursesplatform.repository.CourseRepository;
import com.askel.coursesplatform.repository.UserRepository;
import com.askel.coursesplatform.service.impl.CourseServiceImpl;
import com.askel.coursesplatform.service.mapper.CourseMapper;
import com.askel.coursesplatform.utils.ErrorMessages;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.askel.coursesplatform.utils.TestFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseCache courseCache;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;
    private User instructor;
    private User student;
    private CourseRequestDto courseRequestDto;
    private CourseResponseDto courseResponseDto;

    @BeforeEach
    void setUp() {
        instructor = createUser(1L, "Instructor", "instructor@example.com");
        student = createUser(2L, "Student", "student@example.com");
        course = createCourse(1L, "Java Course", CourseStatus.ACTIVE, instructor);
        courseRequestDto = createCourseRequestDto("Java Course", 1L);
        courseResponseDto = createCourseResponseDto(1L, "Java Course", CourseStatus.ACTIVE);
    }

    @Test
    void getAllCourses_returnsCourses() {
        // Arrange
        List<Course> courses = List.of(course);
        when(courseRepository.findAll()).thenReturn(courses);
        when(courseMapper.toCourseResponseDto(course)).thenReturn(courseResponseDto);

        // Act
        List<CourseResponseDto> result = courseService.getAllCourses();

        // Assert
        assertEquals(1, result.size());
        assertEquals(courseResponseDto, result.getFirst());
        verify(courseRepository).findAll();
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseRepository, courseMapper);
        verifyNoInteractions(userRepository, courseCache);
    }

    @Test
    void getAllCourses_emptyList_returnsEmptyList() {
        // Arrange
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CourseResponseDto> result = courseService.getAllCourses();

        // Assert
        assertTrue(result.isEmpty());
        verify(courseRepository).findAll();
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseMapper, userRepository, courseCache);
    }

    @Test
    void getCourseById_withValidId_returnsCourse() {
        // Arrange
        when(courseCache.get(1L)).thenReturn(null);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.toCourseResponseDto(course)).thenReturn(courseResponseDto);

        // Act
        CourseResponseDto result = courseService.getCourseById(1L);

        // Assert
        assertEquals(courseResponseDto, result);
        verify(courseCache).get(1L);
        verify(courseRepository).findById(1L);
        verify(courseCache).put(1L, course);
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseCache, courseRepository, courseMapper);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCourseById_withCachedCourse_returnsCourse() {
        // Arrange
        when(courseCache.get(1L)).thenReturn(course);
        when(courseMapper.toCourseResponseDto(course)).thenReturn(courseResponseDto);

        // Act
        CourseResponseDto result = courseService.getCourseById(1L);

        // Assert
        assertEquals(courseResponseDto, result);
        verify(courseCache).get(1L);
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseCache, courseMapper);
        verifyNoInteractions(courseRepository, userRepository);
    }

    @Test
    void getCourseById_notFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseCache.get(1L)).thenReturn(null);
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.getCourseById(1L)
        );
        assertEquals(ErrorMessages.COURSE_NOT_FOUND, exception.getMessage());
        verify(courseCache).get(1L);
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseCache, courseRepository);
        verifyNoInteractions(courseMapper, userRepository);
    }

    @Test
    void getCourseByName_withValidName_returnsCourses() {
        // Arrange
        when(courseRepository.findByName("Java Course")).thenReturn(Optional.of(course));
        when(courseMapper.toCourseResponseDto(course)).thenReturn(courseResponseDto);

        // Act
        List<CourseResponseDto> result = courseService.getCourseByName("Java Course");

        // Assert
        assertEquals(1, result.size());
        assertEquals(courseResponseDto, result.getFirst());
        verify(courseRepository).findByName("Java Course");
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseRepository, courseMapper);
        verifyNoInteractions(courseCache, userRepository);
    }

    @Test
    void getCourseByName_noCourses_returnsEmptyList() {
        // Arrange
        when(courseRepository.findByName("Java Course")).thenReturn(Optional.empty());

        // Act
        List<CourseResponseDto> result = courseService.getCourseByName("Java Course");

        // Assert
        assertTrue(result.isEmpty());
        verify(courseRepository).findByName("Java Course");
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseMapper, courseCache, userRepository);
    }

    @Test
    void getCourseByName_nullName_returnsEmptyList() {
        // Arrange
        when(courseRepository.findByName(null)).thenReturn(Optional.empty());

        // Act
        List<CourseResponseDto> result = courseService.getCourseByName(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(courseRepository).findByName(null);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseMapper, courseCache, userRepository);
    }

    @Test
    void getCoursesByStudentId_withValidId_returnsCourses() {
        // Arrange
        course.getStudents().add(student);
        List<Course> courses = List.of(course);
        when(courseRepository.findByStudentId(2L)).thenReturn(courses);
        when(courseMapper.toCourseResponseDto(course)).thenReturn(courseResponseDto);

        // Act
        List<CourseResponseDto> result = courseService.getCoursesByStudentId(2L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(courseResponseDto, result.getFirst());
        verify(courseRepository).findByStudentId(2L);
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseRepository, courseMapper);
        verifyNoInteractions(courseCache, userRepository);
    }

    @Test
    void getCoursesByStudentId_nullId_returnsEmptyList() {
        // Act
        List<CourseResponseDto> result = courseService.getCoursesByStudentId(null);

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(courseRepository, courseMapper, courseCache, userRepository);
    }

    @Test
    void getCoursesByStudentId_noCourses_returnsEmptyList() {
        // Arrange
        when(courseRepository.findByStudentId(2L)).thenReturn(Collections.emptyList());

        // Act
        List<CourseResponseDto> result = courseService.getCoursesByStudentId(2L);

        // Assert
        assertTrue(result.isEmpty());
        verify(courseRepository).findByStudentId(2L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseMapper, courseCache, userRepository);
    }

    @Test
    void getCoursesByStudentId_repositoryReturnsNull_returnsEmptyList() {
        // Arrange
        when(courseRepository.findByStudentId(2L)).thenReturn(null);

        // Act
        List<CourseResponseDto> result = courseService.getCoursesByStudentId(2L);

        // Assert
        assertTrue(result.isEmpty());
        verify(courseRepository).findByStudentId(2L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseMapper, courseCache, userRepository);
    }

    @Test
    void getCoursesByStudentName_withValidName_returnsCourses() {
        // Arrange
        course.getStudents().add(student);
        List<Course> courses = List.of(course);
        when(courseRepository.findAllByStudentName("Student")).thenReturn(courses);
        when(courseMapper.toCourseResponseDto(course)).thenReturn(courseResponseDto);

        // Act
        List<CourseResponseDto> result = courseService.getCoursesByStudentName("Student");

        // Assert
        assertEquals(1, result.size());
        assertEquals(courseResponseDto, result.getFirst());
        verify(courseRepository).findAllByStudentName("Student");
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseRepository, courseMapper);
        verifyNoInteractions(courseCache, userRepository);
    }

    @Test
    void getCoursesByStudentName_noCourses_returnsEmptyList() {
        // Arrange
        when(courseRepository.findAllByStudentName("Student")).thenReturn(Collections.emptyList());

        // Act
        List<CourseResponseDto> result = courseService.getCoursesByStudentName("Student");

        // Assert
        assertTrue(result.isEmpty());
        verify(courseRepository).findAllByStudentName("Student");
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseMapper, courseCache, userRepository);
    }

    @Test
    void getCoursesByInstructorId_withValidId_returnsCourses() {
        // Arrange
        List<Course> courses = List.of(course);
        when(courseRepository.findByInstructorId(1L)).thenReturn(courses);
        when(courseMapper.toCourseResponseDto(course)).thenReturn(courseResponseDto);

        // Act
        List<CourseResponseDto> result = courseService.getCoursesByInstructorId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(courseResponseDto, result.getFirst());
        verify(courseRepository).findByInstructorId(1L);
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseRepository, courseMapper);
        verifyNoInteractions(courseCache, userRepository);
    }

    @Test
    void getCoursesByInstructorId_nullId_returnsEmptyList() {
        // Act
        List<CourseResponseDto> result = courseService.getCoursesByInstructorId(null);

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(courseRepository, courseMapper, courseCache, userRepository);
    }

    @Test
    void getCoursesByInstructorId_noCourses_returnsEmptyList() {
        // Arrange
        when(courseRepository.findByInstructorId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<CourseResponseDto> result = courseService.getCoursesByInstructorId(1L);

        // Assert
        assertTrue(result.isEmpty());
        verify(courseRepository).findByInstructorId(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseMapper, courseCache, userRepository);
    }

    @Test
    void createCourse_withValidData_returnsCreatedCourse() {
        // Arrange
        UserResponseDto instructorDto = createUserResponseDto(1L, "Instructor", "instructor@example.com");
        CourseResponseDto responseDto = createCourseResponseDto(1L, "Java Course", CourseStatus.ACTIVE, instructorDto);
        when(courseRepository.existsByName("Java Course")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseMapper.toCourse(courseRequestDto, instructor)).thenReturn(course);
        when(courseRepository.save(course)).thenReturn(course);
        when(courseMapper.toCourseResponseDto(course)).thenReturn(responseDto);

        // Act
        CourseResponseDto result = courseService.createCourse(courseRequestDto);

        // Assert
        assertEquals(responseDto, result);
        assertTrue(instructor.getTaughtCourses().contains(course));
        verify(courseRepository).existsByName("Java Course");
        verify(userRepository).findById(1L);
        verify(courseMapper).toCourse(courseRequestDto, instructor);
        verify(courseRepository).save(course);
        verify(courseCache).put(1L, course);
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void createCourse_existingCourse_throwsCourseAlreadyExistsException() {
        // Arrange
        when(courseRepository.existsByName("Java Course")).thenReturn(true);

        // Act & Assert
        CourseAlreadyExistsException exception = assertThrows(
                CourseAlreadyExistsException.class,
                () -> courseService.createCourse(courseRequestDto)
        );
        assertEquals("Course with name Java Course already exists", exception.getMessage());
        verify(courseRepository).existsByName("Java Course");
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void createCourse_instructorNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.existsByName("Java Course")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.createCourse(courseRequestDto)
        );
        assertEquals(ErrorMessages.INSTRUCTOR_NOT_FOUND, exception.getMessage());
        verify(courseRepository).existsByName("Java Course");
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void createCoursesBulk_withValidData_returnsCreatedCourses() {
        // Arrange
        List<CourseRequestDto> dtos = List.of(courseRequestDto);
        UserResponseDto instructorDto = createUserResponseDto(1L, "Instructor", "instructor@example.com");
        CourseResponseDto responseDto = createCourseResponseDto(1L, "Java Course", CourseStatus.ACTIVE, instructorDto);
        when(courseRepository.existsByName("Java Course")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseMapper.toCourse(courseRequestDto, instructor)).thenReturn(course);
        when(courseRepository.saveAll(anyList())).thenReturn(List.of(course));
        when(courseMapper.toCourseResponseDto(course)).thenReturn(responseDto);

        // Act
        List<CourseResponseDto> result = courseService.createCourses(dtos);

        // Assert
        assertEquals(1, result.size());
        assertEquals(responseDto, result.getFirst());
        assertTrue(instructor.getTaughtCourses().contains(course));
        verify(courseRepository).existsByName("Java Course");
        verify(userRepository).findById(1L);
        verify(courseMapper).toCourse(courseRequestDto, instructor);
        verify(courseRepository).saveAll(anyList());
        verify(courseCache).put(1L, course);
        verify(courseMapper).toCourseResponseDto(course);
        verifyNoMoreInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void createCoursesBulk_duplicateNames_throwsIllegalArgumentException() {
        // Arrange
        List<CourseRequestDto> dtos = List.of(
                courseRequestDto,
                createCourseRequestDto("Java Course", 1L)
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.createCourses(dtos)
        );
        assertEquals("Course names must be unique", exception.getMessage());
        verifyNoInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void createCoursesBulk_existingCourse_throwsCourseAlreadyExistsException() {
        // Arrange
        List<CourseRequestDto> dtos = List.of(courseRequestDto);
        when(courseRepository.existsByName("Java Course")).thenReturn(true);

        // Act & Assert
        CourseAlreadyExistsException exception = assertThrows(
                CourseAlreadyExistsException.class,
                () -> courseService.createCourses(dtos)
        );
        assertTrue(exception.getMessage().contains("Java Course"));
        verify(courseRepository).existsByName("Java Course");
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void createCoursesBulk_instructorNotFound_throwsEntityNotFoundException() {
        // Arrange
        List<CourseRequestDto> dtos = List.of(courseRequestDto);
        when(courseRepository.existsByName("Java Course")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.createCourses(dtos)
        );
        assertEquals(ErrorMessages.INSTRUCTOR_NOT_FOUND, exception.getMessage());
        verify(courseRepository).existsByName("Java Course");
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void updateCourse_withValidDataSameInstructor_returnsUpdatedCourse() {
        // Arrange
        CourseRequestDto updateDto = createCourseRequestDto("Updated Course", 1L);
        UserResponseDto instructorDto = createUserResponseDto(1L, "Instructor", "instructor@example.com");
        CourseResponseDto updatedResponseDto = createCourseResponseDto(1L, "Updated Course", CourseStatus.ACTIVE, instructorDto);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseMapper.toCourseResponseDto(any(Course.class))).thenReturn(updatedResponseDto);

        // Act
        CourseResponseDto result = courseService.updateCourse(1L, updateDto);

        // Assert
        assertEquals(updatedResponseDto, result);
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
        verify(courseCache).put(eq(1L), any(Course.class)); // Исправлено: 1L -> eq(1L)
        verify(courseMapper).toCourseResponseDto(any(Course.class));
        verifyNoMoreInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void updateCourse_withNewInstructor_updatesInstructor() {
        // Arrange
        User newInstructor = createUser(3L, "New Instructor", "new@example.com");
        UserResponseDto newInstructorDto = createUserResponseDto(3L, "New Instructor", "new@example.com");
        CourseRequestDto updateDto = createCourseRequestDto("Updated Course", 3L);
        CourseResponseDto updatedResponseDto = createCourseResponseDto(1L, "Updated Course", CourseStatus.ACTIVE, newInstructorDto);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newInstructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseMapper.toCourseResponseDto(any(Course.class))).thenReturn(updatedResponseDto);

        // Act
        CourseResponseDto result = courseService.updateCourse(1L, updateDto);

        // Assert
        assertEquals(updatedResponseDto, result);
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(3L);
        verify(courseRepository).save(any(Course.class));
        verify(courseCache).put(eq(1L), any(Course.class)); // Исправлено: 1L -> eq(1L)
        verify(courseMapper).toCourseResponseDto(any(Course.class));
        verifyNoMoreInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void updateCourse_courseNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.updateCourse(1L, courseRequestDto)
        );
        assertEquals(ErrorMessages.COURSE_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void updateCourse_instructorNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.updateCourse(1L, courseRequestDto)
        );
        assertEquals(ErrorMessages.INSTRUCTOR_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void deleteCourseById_withValidId_deletesCourse() {
        // Arrange
        course.getStudents().add(student);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        doNothing().when(courseRepository).delete(course);

        // Act
        courseService.deleteCourseById(1L);

        // Assert
        assertTrue(student.getEnrolledCourses().isEmpty());
        assertTrue(instructor.getTaughtCourses().isEmpty());
        verify(courseRepository).findById(1L);
        verify(courseRepository).delete(course);
        verify(courseCache).remove(1L);
        verifyNoMoreInteractions(courseRepository, courseCache);
        verifyNoInteractions(userRepository, courseMapper);
    }

    @Test
    void deleteCourseById_notFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.deleteCourseById(1L)
        );
        assertEquals(ErrorMessages.COURSE_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(courseCache, userRepository, courseMapper);
    }

    @Test
    void addStudentToCourse_withValidData_addsStudent() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        // Act
        courseService.addStudentToCourse(1L, 2L);

        // Assert
        assertTrue(course.getStudents().contains(student));
        assertTrue(student.getEnrolledCourses().contains(course));
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(courseCache).put(1L, course);
        verifyNoMoreInteractions(courseRepository, userRepository, courseCache);
        verifyNoInteractions(courseMapper);
    }

    @Test
    void addStudentToCourse_courseNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.addStudentToCourse(1L, 2L)
        );
        assertEquals(ErrorMessages.COURSE_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void addStudentToCourse_studentNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.addStudentToCourse(1L, 2L)
        );
        assertEquals(ErrorMessages.STUDENT_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void addStudentToCourse_alreadyEnrolled_throwsIllegalArgumentException() {
        // Arrange
        course.getStudents().add(student);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.addStudentToCourse(1L, 2L)
        );
        assertEquals("Student is already enrolled in this course", exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void removeStudentFromCourse_withValidData_removesStudent() {
        // Arrange
        course.getStudents().add(student);
        student.getEnrolledCourses().add(course);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        // Act
        courseService.removeStudentFromCourse(1L, 2L);

        // Assert
        assertFalse(course.getStudents().contains(student));
        assertFalse(student.getEnrolledCourses().contains(course));
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(courseCache).put(1L, course);
        verifyNoMoreInteractions(courseRepository, userRepository, courseCache);
        verifyNoInteractions(courseMapper);
    }

    @Test
    void removeStudentFromCourse_courseNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.removeStudentFromCourse(1L, 2L)
        );
        assertEquals(ErrorMessages.COURSE_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void removeStudentFromCourse_studentNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.removeStudentFromCourse(1L, 2L)
        );
        assertEquals(ErrorMessages.STUDENT_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void removeStudentFromCourse_notEnrolled_throwsIllegalArgumentException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> courseService.removeStudentFromCourse(1L, 2L)
        );
        assertEquals("Student is not enrolled in this course", exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void assignInstructorToCourse_withValidData_assignsInstructor() {
        // Arrange
        Course pendingCourse = createCourse(1L, "Pending Course", CourseStatus.PENDING_INSTRUCTOR, null);
        UserResponseDto instructorDto = createUserResponseDto(1L, "Instructor", "instructor@example.com");
        CourseResponseDto updatedResponseDto = createCourseResponseDto(1L, "Pending Course", CourseStatus.ACTIVE, instructorDto);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(pendingCourse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseMapper.toCourseResponseDto(any(Course.class))).thenReturn(updatedResponseDto);

        // Act
        CourseResponseDto result = courseService.assignInstructorToCourse(1L, 1L);

        // Assert
        assertEquals(updatedResponseDto, result);
        assertEquals(instructor, pendingCourse.getInstructor());
        assertEquals(CourseStatus.ACTIVE, pendingCourse.getStatus());
        assertTrue(instructor.getTaughtCourses().contains(pendingCourse));
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(courseRepository).save(pendingCourse);
        verify(courseCache).put(1L, pendingCourse);
        verify(courseMapper).toCourseResponseDto(pendingCourse);
        verifyNoMoreInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void assignInstructorToCourse_courseNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.assignInstructorToCourse(1L, 1L)
        );
        assertEquals(ErrorMessages.COURSE_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void assignInstructorToCourse_instructorNotFound_throwsEntityNotFoundException() {
        // Arrange
        Course pendingCourse = createCourse(1L, "Pending Course", CourseStatus.PENDING_INSTRUCTOR, null);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(pendingCourse));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.assignInstructorToCourse(1L, 1L)
        );
        assertEquals(ErrorMessages.INSTRUCTOR_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void assignInstructorToCourse_wrongStatus_throwsIllegalStateException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> courseService.assignInstructorToCourse(1L, 1L)
        );
        assertEquals("Instructor can only be assigned to a course with PENDING_INSTRUCTOR status", exception.getMessage());
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void assignInstructorToCourse_replaceExistingInstructor_updatesRelations() {
        // Arrange
        User oldInstructor = new User();
        oldInstructor.setId(2L);
        oldInstructor.setName("Old Instructor");

        Course pendingCourse = createCourse(1L, "Pending Course", CourseStatus.PENDING_INSTRUCTOR, oldInstructor);
        oldInstructor.getTaughtCourses().add(pendingCourse);

        User newInstructor = instructor;
        CourseResponseDto updatedResponseDto = createCourseResponseDto(1L, "Pending Course", CourseStatus.ACTIVE,
                createUserResponseDto(1L, "Instructor", "instructor@example.com"));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(pendingCourse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(newInstructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseMapper.toCourseResponseDto(any(Course.class))).thenReturn(updatedResponseDto);

        // Act
        CourseResponseDto result = courseService.assignInstructorToCourse(1L, 1L);

        // Assert
        assertEquals(updatedResponseDto, result);
        assertEquals(newInstructor, pendingCourse.getInstructor());
        assertEquals(CourseStatus.ACTIVE, pendingCourse.getStatus());
        assertTrue(newInstructor.getTaughtCourses().contains(pendingCourse));
        assertFalse(oldInstructor.getTaughtCourses().contains(pendingCourse));
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(courseRepository).save(pendingCourse);
        verify(courseCache).put(1L, pendingCourse);
        verify(courseMapper).toCourseResponseDto(pendingCourse);
        verifyNoMoreInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void assignInstructorToCourse_sameInstructor_noChanges() {
        // Arrange
        Course pendingCourse = createCourse(1L, "Pending Course", CourseStatus.PENDING_INSTRUCTOR, instructor);
        instructor.getTaughtCourses().add(pendingCourse);

        CourseResponseDto updatedResponseDto = createCourseResponseDto(1L, "Pending Course", CourseStatus.ACTIVE,
                createUserResponseDto(1L, "Instructor", "instructor@example.com"));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(pendingCourse));
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(courseMapper.toCourseResponseDto(any(Course.class))).thenReturn(updatedResponseDto);

        // Act
        CourseResponseDto result = courseService.assignInstructorToCourse(1L, 1L);

        // Assert
        assertEquals(updatedResponseDto, result);
        assertEquals(instructor, pendingCourse.getInstructor());
        assertEquals(CourseStatus.ACTIVE, pendingCourse.getStatus());
        assertTrue(instructor.getTaughtCourses().contains(pendingCourse));
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(courseRepository).save(pendingCourse);
        verify(courseCache).put(1L, pendingCourse);
        verify(courseMapper).toCourseResponseDto(pendingCourse);
        verifyNoMoreInteractions(courseRepository, userRepository, courseMapper, courseCache);
    }

    @Test
    void unassignInstructorFromCourse_withValidData_unassignsInstructor() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));

        // Act
        courseService.unassignInstructorFromCourse(1L, 1L);

        // Assert
        assertNull(course.getInstructor());
        assertEquals(CourseStatus.PENDING_INSTRUCTOR, course.getStatus());
        assertFalse(instructor.getTaughtCourses().contains(course));
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(courseCache).put(1L, course);
        verifyNoMoreInteractions(courseRepository, userRepository, courseCache);
        verifyNoInteractions(courseMapper);
    }

    @Test
    void unassignInstructorFromCourse_courseNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.unassignInstructorFromCourse(1L, 1L)
        );
        assertEquals(ErrorMessages.COURSE_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository);
        verifyNoInteractions(userRepository, courseMapper, courseCache);
    }

    @Test
    void unassignInstructorFromCourse_instructorNotFound_throwsEntityNotFoundException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> courseService.unassignInstructorFromCourse(1L, 1L)
        );
        assertEquals(ErrorMessages.INSTRUCTOR_NOT_FOUND, exception.getMessage());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(courseRepository, userRepository);
        verifyNoInteractions(courseMapper, courseCache);
    }

    @Test
    void unassignInstructorFromCourse_noInstructor_completesNormally() {
        // Arrange
        Course courseWithoutInstructor = createCourse(1L, "Course", CourseStatus.ACTIVE, null);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(courseWithoutInstructor));
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));

        // Act
        courseService.unassignInstructorFromCourse(1L, 1L);

        // Assert
        assertNull(courseWithoutInstructor.getInstructor());
        assertEquals(CourseStatus.PENDING_INSTRUCTOR, courseWithoutInstructor.getStatus());
        verify(courseRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(courseCache).put(1L, courseWithoutInstructor);
        verifyNoMoreInteractions(courseRepository, userRepository, courseCache);
        verifyNoInteractions(courseMapper);
    }
}