package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import com.askel.coursesplatform.model.entity.Course;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.CourseStatus;
import com.askel.coursesplatform.repository.CourseRepository;
import com.askel.coursesplatform.repository.UserRepository;
import com.askel.coursesplatform.service.UserService;
import com.askel.coursesplatform.service.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID "
                        + id + " not found"));
        return userMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = userMapper.toUser(userRequestDto);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID"
                        + id + "not found"));

        user.setName(userRequestDto.name());
        user.setEmail(userRequestDto.email());

        User uodatedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(uodatedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID"
                        + id + "not found"));

        for (Course course : user.getEnrolledCourses()) {
            course.getStudents().remove(user);
        }

        for (Course course : user.getTaughtCourses()) {
            course.setInstructor(null); //for now
            course.setStatus(CourseStatus.PENDING_INSTRUCTOR);
            courseRepository.save(course);
        }

        userRepository.deleteById(id);
    }
}
