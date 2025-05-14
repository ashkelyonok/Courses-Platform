package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.cache.UserCache;
import com.askel.coursesplatform.exception.ResourceNotFoundException;
import com.askel.coursesplatform.factory.UserDtoFactory;
import com.askel.coursesplatform.model.dto.UserResponse;
import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.CourseResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.CourseStatus;
import com.askel.coursesplatform.model.enums.UserRoles;
import com.askel.coursesplatform.repository.CourseRepository;
import com.askel.coursesplatform.repository.UserRepository;
import com.askel.coursesplatform.service.UserService;
import com.askel.coursesplatform.service.mapper.CourseMapper;
import com.askel.coursesplatform.service.mapper.UserMapper;
import com.askel.coursesplatform.utils.ErrorMessages;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCache userCache;
    private final UserMapper userMapper;
    private final UserDtoFactory userDtoFactory;

    private final CourseMapper courseMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userDtoFactory::create)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userCache.get(id);
        if (user != null) {
            return userDtoFactory.create(user);
        }

        user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        userCache.put(id, user);
        return userDtoFactory.create(user);
    }

    @Override
    public List<UserResponse> getUsersByRole(UserRoles role) {
        return userRepository.findByRole(role).stream()
                .map(userDtoFactory::create)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequestDto userRequestDto) {
        User user = userMapper.toUserFromDto(userRequestDto);
        //user.setRole(UserRoles.USER); // По умолчанию

        User savedUser = userRepository.save(user);
        userCache.put(savedUser.getId(), savedUser);
        return userDtoFactory.create(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setName(userRequestDto.name());
        user.setEmail(userRequestDto.email());

        if (userRequestDto.password() != null &&
                !userRequestDto.password().equals("KEEP_CURRENT_PASSWORD")) {
            user.setPassword(passwordEncoder.encode(userRequestDto.password()));
        }

        User updatedUser = userRepository.save(user);
        userCache.put(id, updatedUser);
        return userDtoFactory.create(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Обработка связанных курсов
        processEnrolledCourses(user);
        processTaughtCourses(user);

        userRepository.delete(user);
        userCache.remove(id);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));
        return userDtoFactory.create(user);
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<UserResponse> getUsersEnrolledInCourse(Long courseId) {
        return userRepository.findByEnrolledCoursesId(courseId).stream()
                .map(userDtoFactory::create)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersTeachingCourse(Long courseId) {
        return userRepository.findByTaughtCoursesId(courseId).stream()
                .map(userDtoFactory::create)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByName(String namePart) {
        return userRepository.findByNameContainingIgnoreCase(namePart).stream()
                .map(userDtoFactory::create)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByRoleAndName(UserRoles role, String namePart) {
        return userRepository.findByRoleAndNameContainingIgnoreCase(role, namePart).stream()
                .map(userDtoFactory::create)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse changeUserRole(Long userId, UserRoles newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        userCache.put(userId, updatedUser);
        return userDtoFactory.create(updatedUser);
    }

    @Override
    public List<CourseResponseDto> getCoursesEnrolledByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Админ может быть студентом, поэтому проверяем, записан ли он куда-то
//        if (user.getEnrolledCourses().isEmpty() && user.getRole() != UserRoles.ADMIN) {
//            throw new ResourceNotFoundException("User is not enrolled in any courses");
//        }

        List<Course> courses = userRepository.findEnrolledCoursesByUserId(userId);
        return courses.stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseResponseDto> getCoursesTaughtByUser(Long userId) {
        userId+=0L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Админ может быть инструктором, поэтому проверяем, ведет ли он курсы
//        if (user.getTaughtCourses().isEmpty() && user.getRole() != UserRoles.ADMIN) {
//            throw new ResourceNotFoundException("User is not teaching any courses");
//        }

        List<Course> courses = userRepository.findTaughtCoursesByUserId(userId);
        return courses.stream()
                .map(courseMapper::toCourseResponseDto)
                .collect(Collectors.toList());
    }

    private void processEnrolledCourses(User user) {
        user.getEnrolledCourses().forEach(course ->
                course.getStudents().remove(user)
        );
    }

    private void processTaughtCourses(User user) {
        user.getTaughtCourses().forEach(course -> {
            course.setInstructor(null);
            course.setStatus(CourseStatus.PENDING_INSTRUCTOR);
            courseRepository.save(course);
        });
    }
}