package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.cache.CourseCache;
import com.askel.coursesplatform.exception.CourseAlreadyExistsException;
import com.askel.coursesplatform.exception.ResourceNotFoundException;
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
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserRepository userRepository;
    private final CourseCache courseCache;

    private void initializeCourseAssociations(Course course) {
        // Initialize instructor and its taughtCourses
        if (course.getInstructor() != null) {
            Hibernate.initialize(course.getInstructor());
            Hibernate.initialize(course.getInstructor().getTaughtCourses());
            Hibernate.initialize(course.getInstructor().getEnrolledCourses());
        }
        // Initialize students and their enrolledCourses/taughtCourses
        Hibernate.initialize(course.getStudents());
        for (User student : course.getStudents()) {
            Hibernate.initialize(student.getEnrolledCourses());
            Hibernate.initialize(student.getTaughtCourses());
        }
    }

    private void initializeCourseList(List<Course> courses) {
        if (courses == null) return;
        for (Course course : courses) {
            initializeCourseAssociations(course);
        }
    }

    @Override
    public List<CourseResponseDto> getAllCourses() {
        log.info("Fetching all courses");
        List<CourseResponseDto> courses = courseRepository.findAll().stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
        log.debug("Found {} courses", courses.size());
        return courses;
    }

    @Override
    public CourseResponseDto getCourseById(Long id) {
        log.info("Fetching course by id: {}", id);
        Course cachedCourse = courseCache.get(id);
        if (cachedCourse != null) {
            log.debug("Course with id {} retrieved from cache", id);
            initializeCourseAssociations(cachedCourse);
            return courseMapper.toCourseResponseDto(cachedCourse);
        }

//        Course course = courseRepository.findById(id)
//                .orElseThrow(() -> {
//                    log.error("Course not found with id: {}", id);
//                    return new ResourceNotFoundException(ErrorMessages.COURSE_NOT_FOUND);
//                });

        Course course = courseRepository.findByIdWithDetails(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", id);
                    return new ResourceNotFoundException("Course not found with id: " + id);
                });
        initializeCourseAssociations(course);
        courseCache.put(id, course);
        log.info("Course with id {} retrieved from DB and cached", id);
        return courseMapper.toCourseResponseDto(course);
    }

    @Override
    public List<CourseResponseDto> getCourseByName(String name) {
        log.info("Searching for course by name: {}", name);
        List<CourseResponseDto> result = courseRepository.findByName(name)
                .map(course -> List.of(courseMapper.toCourseResponseDto(course)))
                .orElse(Collections.emptyList());
        log.debug("Found {} courses for name: {}", result.size(), name);
        return result;
    }

    @Override
    public List<CourseResponseDto> getCoursesByStudentId(Long studentId) {
        log.info("Fetching courses for student with id: {}", studentId);

        if (studentId == null) {
            log.warn("Null studentId provided");
            return List.of();
        }

        List<Course> courses = courseRepository.findByStudentId(studentId);

        if (courses == null) {
            log.warn("Repository returned null courses for studentId: {}", studentId);
            return List.of();
        }

        initializeCourseList(courses);
        log.debug("Found {} courses for student {}", courses.size(), studentId);

        return courses.stream()
                .filter(Objects::nonNull) // защита от null-курсов
                .map(courseMapper::toCourseResponseDto)
                .toList();
    }

    @Override
    public List<CourseResponseDto> getCoursesByStudentName(String studentName) {
        log.info("Fetching courses for student with name: '{}'", studentName);
        List<Course> courses = courseRepository.findAllByStudentName(studentName);

        if (log.isDebugEnabled()) {
            courses.forEach(c ->
                    log.debug("Course id={} has student with name '{}'", c.getId(), studentName)
            );
        }

        List<CourseResponseDto> result = courses.stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
        log.info("Found {} courses for student '{}'", result.size(), studentName);
        return result;
    }

    @Override
    public List<CourseResponseDto> getCoursesByInstructorId(Long instructorId) {
        log.info("Fetching courses taught by instructor with id: {}", instructorId);

        if (instructorId == null) {
            log.warn("Null instructorId provided");
            return List.of();
        }

        List<Course> courses = courseRepository.findByInstructorId(instructorId);

        initializeCourseList(courses);
        log.debug("Instructor {} teaches {} courses",
                instructorId, courses == null ? 0 : courses.size());

        return courses == null ? List.of() : courses.stream()
                .filter(Objects::nonNull)
                .map(courseMapper::toCourseResponseDto)
                .toList();
    }

    @Override
    public List<CourseResponseDto> getCoursesByInstructorName(String instructorName) {
        log.info("Fetching courses for instructor with name: '{}'", instructorName);
        List<Course> courses = courseRepository.findByInstructorName(instructorName);

        if (log.isDebugEnabled()) {
            courses.forEach(c ->
                    log.debug("Course id={} has instructor with name '{}'", c.getId(), instructorName)
            );
        }

        List<CourseResponseDto> result = courses.stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
        log.info("Found {} courses for instructor '{}'", result.size(), instructorName);
        return result;
    }

    @Override
    @Transactional
    public CourseResponseDto createCourse(CourseRequestDto courseRequestDto) {
        log.info("Creating new course with name: '{}'", courseRequestDto.name());

        if (courseRepository.existsByName(courseRequestDto.name())) {
            log.warn("Course with name '{}' already exists", courseRequestDto.name());
            throw new CourseAlreadyExistsException(
                    "Course with name " + courseRequestDto.name() + " already exists");
        }

        User instructor = userRepository.findById(courseRequestDto.instructorId())
                .orElseThrow(() -> {
                    log.error("Instructor not found with id: {}", courseRequestDto.instructorId());
                    return new EntityNotFoundException(ErrorMessages.INSTRUCTOR_NOT_FOUND);
                });

        Course course = courseMapper.toCourse(courseRequestDto, instructor);
        course.setStatus(CourseStatus.ACTIVE);

        log.debug("Saving new course with instructor {}", instructor.getId());
        Course savedCourse = courseRepository.save(course);

        instructor.getTaughtCourses().add(savedCourse);
        initializeCourseAssociations(savedCourse);
        courseCache.put(savedCourse.getId(), savedCourse);

        log.info("Successfully created course with id: {}", savedCourse.getId());
        return courseMapper.toCourseResponseDto(savedCourse);
    }

    @Override
    @Transactional
    public List<CourseResponseDto> createCourses(List<CourseRequestDto> courseRequestDtos) {
        log.info("Starting bulk creation of {} courses", courseRequestDtos.size());

        // Проверка дубликатов имен в запросе
        Set<String> courseNames = courseRequestDtos.stream()
                .map(CourseRequestDto::name)
                .collect(Collectors.toSet());
        if (courseNames.size() != courseRequestDtos.size()) {
            log.warn("Duplicate course names detected: {}", courseNames);
            throw new IllegalArgumentException("Course names must be unique");
        }

        // Проверка существующих курсов
        List<String> existingNames = courseNames.stream()
                .filter(courseRepository::existsByName)
                .toList();
        if (!existingNames.isEmpty()) {
            log.warn("Courses already exist: {}", existingNames);
            throw new CourseAlreadyExistsException("Courses already exist: " + existingNames);
        }

        // Загрузка инструкторов
        Map<Long, User> instructors = courseRequestDtos.stream()
                .map(CourseRequestDto::instructorId)
                .distinct()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException(
                                        ErrorMessages.INSTRUCTOR_NOT_FOUND))
                ));

        // Создание и сохранение курсов
        List<Course> savedCourses = courseRepository.saveAll(
                courseRequestDtos.stream()
                        .map(dto -> {
                            User instructor = instructors.get(dto.instructorId());
                            Course course = courseMapper.toCourse(dto, instructor);
                            course.setStatus(CourseStatus.ACTIVE);
                            return course;
                        })
                        .toList()
        );

        // Обновление кэша и taughtCourses
        savedCourses.forEach(course -> {
            courseCache.put(course.getId(), course);
            User instructor = course.getInstructor();
            if (instructor != null && !instructor.getTaughtCourses().contains(course)) {
                instructor.getTaughtCourses().add(course);
            }
        });

        log.info("Created {} courses", savedCourses.size());
        return savedCourses.stream()
                .map(courseMapper::toCourseResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public CourseResponseDto updateCourse(Long id, CourseRequestDto courseRequestDto) {
        log.info("Updating course with id: {}", id);

        Course course = courseRepository.findByIdWithDetails(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", id);
                    return new ResourceNotFoundException(ErrorMessages.COURSE_NOT_FOUND);
                });

        course.setName(courseRequestDto.name());
        course.setDescription(courseRequestDto.description());

        User oldInstructor = course.getInstructor();
        User newInstructor = null;
        if (courseRequestDto.instructorId() != null) {
            newInstructor = userRepository.findById(courseRequestDto.instructorId())
                    .orElseThrow(() -> {
                        log.error("Instructor not found with id: {}", courseRequestDto.instructorId());
                        return new ResourceNotFoundException("Instructor not found");
                    });
        }


//        if (!oldInstructor.equals(newInstructor)) {
//            log.debug("Changing instructor from {} to {} for course {}",
//                    oldInstructor.getId(), newInstructor.getId(), id);
//            oldInstructor.getTaughtCourses().remove(course);
//            newInstructor.getTaughtCourses().add(course);
//            course.setInstructor(newInstructor);
//        }

        if (newInstructor != null) {
            // Assign new instructor
            if (oldInstructor == null || !oldInstructor.equals(newInstructor)) {
                log.debug("Changing instructor from {} to {} for course {}",
                        oldInstructor != null ? oldInstructor.getId() : "none",
                        newInstructor.getId(), id);
                if (oldInstructor != null) {
                    Hibernate.initialize(oldInstructor);
                    Hibernate.initialize(oldInstructor.getTaughtCourses());
                    oldInstructor.getTaughtCourses().remove(course);
                }
                Hibernate.initialize(newInstructor);
                Hibernate.initialize(newInstructor.getTaughtCourses());
                newInstructor.getTaughtCourses().add(course);
                course.setInstructor(newInstructor);
                course.setStatus(CourseStatus.ACTIVE);
            }
        } else {
            // Unassign instructor
            if (oldInstructor != null) {
                log.debug("Unassigning instructor {} from course {}", oldInstructor.getId(), id);
                Hibernate.initialize(oldInstructor);
                Hibernate.initialize(oldInstructor.getTaughtCourses());
                oldInstructor.getTaughtCourses().remove(course);
                course.setInstructor(null);
                course.setStatus(CourseStatus.PENDING_INSTRUCTOR);
            }
        }

        Course updatedCourse = courseRepository.save(course);
        initializeCourseAssociations(updatedCourse);
        courseCache.put(id, updatedCourse);

        log.info("Successfully updated course with id: {}", id);
        return courseMapper.toCourseResponseDto(updatedCourse);
    }

    @Override
    @Transactional
    public void deleteCourseById(Long id) {
        log.warn("Attempting to delete course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", id);
                    return new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND);
                });

        int studentCount = course.getStudents().size();
        log.debug("Removing {} students from course {}", studentCount, id);

        course.getStudents().forEach(student -> {
            log.trace("Removed course {} from student {}", id, student.getId());
            Hibernate.initialize(student);
            Hibernate.initialize(student.getEnrolledCourses());
            student.getEnrolledCourses().remove(course);
        });
        course.getStudents().clear();

        User instructor = course.getInstructor();
        if (instructor != null) {
            log.debug("Removing instructor {} from course {}", instructor.getId(), id);
            Hibernate.initialize(instructor);
            Hibernate.initialize(instructor.getTaughtCourses());
            instructor.getTaughtCourses().remove(course);
        }

        courseCache.remove(id);
        courseRepository.delete(course);
        log.info("Successfully deleted course with id: {}", id);
    }

    @Override
    @Transactional
    public void addStudentToCourse(Long courseId, Long studentId) {
        log.info("Adding student {} to course {}", studentId, courseId);

        Course course = courseRepository.findByIdWithDetails(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", courseId);
                    return new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND);
                });

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Student not found with id: {}", studentId);
                    return new EntityNotFoundException(ErrorMessages.STUDENT_NOT_FOUND);
                });

        if (course.getStudents().contains(student)) {
            log.warn("Student {} is already enrolled in course {}", studentId, courseId);
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        course.getStudents().add(student);
        student.getEnrolledCourses().add(course);

        initializeCourseAssociations(course);
        courseCache.put(courseId, course);

        log.info("Successfully added student {} to course {}", studentId, courseId);
    }

    @Override
    @Transactional
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        log.info("Removing student {} from course {}", studentId, courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", courseId);
                    return new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND);
                });

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.error("Student not found with id: {}", studentId);
                    return new EntityNotFoundException(ErrorMessages.STUDENT_NOT_FOUND);
                });

        if (!course.getStudents().contains(student)) {
            log.warn("Student {} is not enrolled in course {}", studentId, courseId);
            throw new IllegalArgumentException("Student is not enrolled in this course");
        }

        course.getStudents().remove(student);
        student.getEnrolledCourses().remove(course);

        initializeCourseAssociations(course);
        courseCache.put(courseId, course);

        log.info("Successfully removed student {} from course {}", studentId, courseId);
    }

    @Override
    @Transactional
    public CourseResponseDto assignInstructorToCourse(Long courseId, Long instructorId) {
        log.info("Assigning instructor {} to course {}", instructorId, courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", courseId);
                    return new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND);
                });

        if (course.getStatus() != CourseStatus.PENDING_INSTRUCTOR) {
            log.warn("Cannot assign instructor to course {} with status {}",
                    courseId, course.getStatus());
            throw new IllegalStateException("Instructor can only be assigned to a course "
                    + "with PENDING_INSTRUCTOR status");
        }

        User newInstructor = userRepository.findById(instructorId)
                .orElseThrow(() -> {
                    log.error("Instructor not found with id: {}", instructorId);
                    return new EntityNotFoundException(ErrorMessages.INSTRUCTOR_NOT_FOUND);
                });

        User oldInstructor = course.getInstructor();
        if (oldInstructor != null && !oldInstructor.equals(newInstructor)) {
            log.debug("Replacing instructor {} with {} for course {}",
                    oldInstructor.getId(), newInstructor.getId(), courseId);
            Hibernate.initialize(oldInstructor);
            Hibernate.initialize(oldInstructor.getTaughtCourses());
            oldInstructor.getTaughtCourses().remove(course);
        }

        course.setInstructor(newInstructor);
        course.setStatus(CourseStatus.ACTIVE);
        newInstructor.getTaughtCourses().add(course);

        Course updatedCourse = courseRepository.save(course);
        initializeCourseAssociations(updatedCourse);

        courseCache.put(courseId, updatedCourse);

        log.info("Successfully assigned instructor {} to course {}", instructorId, courseId);
        return courseMapper.toCourseResponseDto(updatedCourse);
    }

    @Override
    @Transactional
    public void unassignInstructorFromCourse(Long courseId, Long instructorId) {
        log.info("Unassigning instructor {} from course {}", instructorId, courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    log.error("Course not found with id: {}", courseId);
                    return new EntityNotFoundException(ErrorMessages.COURSE_NOT_FOUND);
                });

        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> {
                    log.error("Instructor not found with id: {}", instructorId);
                    return new EntityNotFoundException(ErrorMessages.INSTRUCTOR_NOT_FOUND);
                });

        if (instructor == null) {
            throw new IllegalStateException("Course already has no instructor");
        }

        course.setInstructor(null);
        course.setStatus(CourseStatus.PENDING_INSTRUCTOR);

        Hibernate.initialize(instructor);
        Hibernate.initialize(instructor.getTaughtCourses());

        instructor.getTaughtCourses().remove(course);
        initializeCourseAssociations(course);

        courseCache.put(courseId, course);
        log.info("Successfully unassigned instructor {} from course {}", instructorId, courseId);
    }

    @Override
    public List<CourseResponseDto> getCoursesByStatus(CourseStatus status) {
        return courseRepository.findByStatus(status).stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesByDescription(String descriptionPart) {
        return courseRepository.findByDescriptionContainingIgnoreCase(descriptionPart).stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesByStatusAndName(CourseStatus status, String namePart) {
        return courseRepository.findByStatusAndNameContainingIgnoreCase(status, namePart).stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesWithoutStudents() {
        return courseRepository.findByStudentsEmpty().stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesWithoutInstructor() {
        return courseRepository.findByInstructorIsNull().stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesWithInstructor() {
        log.info("Getting courses with instructor");
        return courseRepository.findWithInstructor().stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesWithStudents() {
        log.info("Getting courses with students");
        return courseRepository.findWithStudents().stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }
}
