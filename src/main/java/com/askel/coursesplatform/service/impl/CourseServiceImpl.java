package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.model.dto.request.CourseRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.CourseStatus;
import com.askel.coursesplatform.repository.CourseRepository;
import com.askel.coursesplatform.repository.UserRepository;
import com.askel.coursesplatform.service.CourseService;
import com.askel.coursesplatform.service.mapper.CourseMapper;
import com.askel.coursesplatform.utils.ErrorMessages;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserRepository userRepository;

    @Override
    public List<CourseResponseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
    }

    @Override
    public CourseResponseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND));
        return courseMapper.toCourseResponseDto(course);
    }

    @Override
    public List<CourseResponseDto> getCourseByName(String name) {
        return courseRepository.findByName(name).stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
    }

    @Override
    public List<CourseResponseDto> getCoursesByStudentId(Long studentId) {
        return courseRepository.findByStudentId(studentId).stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
    }

    @Override
    public List<CourseResponseDto> getCoursesByInstructorId(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public CourseResponseDto createCourse(CourseRequestDto courseRequestDto) {
        User instructor = userRepository.findById(courseRequestDto.instructorId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.INSTRUCTOR_NOT_FOUND));

        Course course = courseMapper.toCourse(courseRequestDto, instructor);
        course.setStatus(CourseStatus.ACTIVE);
        Course savedCourse = courseRepository.save(course);

        instructor.getTaughtCourses().add(savedCourse);

        return courseMapper.toCourseResponseDto(savedCourse);
    }

    @Override
    @Transactional
    public CourseResponseDto updateCourse(Long id, CourseRequestDto courseRequestDto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND));

        User oldInstructor = course.getInstructor();
        User newInstructor = userRepository.findById(courseRequestDto.instructorId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                ErrorMessages.INSTRUCTOR_NOT_FOUND));

        course.setName(courseRequestDto.name());
        course.setDescription(courseRequestDto.description());

        if (!oldInstructor.equals(newInstructor)) {
            oldInstructor.getTaughtCourses().remove(course);

            newInstructor.getTaughtCourses().add(course);

            course.setInstructor(newInstructor);
        }

        Course updatedCourse = courseRepository.save(course);
        return courseMapper.toCourseResponseDto(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND));

        for (User student : course.getStudents()) {
            student.getEnrolledCourses().remove(course);
        }
        course.getStudents().clear();

        User instructor = course.getInstructor();
        if (instructor != null) {
            instructor.getTaughtCourses().remove(course);
        }

        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public void addStudentToCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.STUDENT_NOT_FOUND));

        if (course.getStudents().contains(student)) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        course.getStudents().add(student);
        student.getEnrolledCourses().add(course);
    }

    @Override
    @Transactional
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.STUDENT_NOT_FOUND));

        if (!course.getStudents().contains(student)) {
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }

        course.getStudents().remove(student);

        student.getEnrolledCourses().remove(course);
    }

    @Override
    @Transactional
    public CourseResponseDto assignInstructorToCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND));

        if (course.getStatus() != CourseStatus.PENDING_INSTRUCTOR) {
            throw new IllegalStateException("Instructor can only be assigned to a course "
                    + "with PENDING_INSTRUCTOR status");
        }

        User newInstructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.INSTRUCTOR_NOT_FOUND));

        User oldInstructor = course.getInstructor();
        if (oldInstructor != null && !oldInstructor.equals(newInstructor)) {
            oldInstructor.getTaughtCourses().remove(course);
        }

        course.setInstructor(newInstructor);
        course.setStatus(CourseStatus.ACTIVE);
        newInstructor.getTaughtCourses().add(course);

        Course updatedCourse = courseRepository.save(course);

        return courseMapper.toCourseResponseDto(updatedCourse);
    }

    @Override
    @Transactional
    public void unassignInstructorFromCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND));

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.INSTRUCTOR_NOT_FOUND));

        if (instructor == null) {
            throw new IllegalStateException("Course already has no instructor");
        }

        course.setInstructor(null);
        course.setStatus(CourseStatus.PENDING_INSTRUCTOR);

        instructor.getTaughtCourses().remove(course);
    }
}
