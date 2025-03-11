package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.dto.request.UserRequestDto;
import com.askel.coursesplatform.model.dto.response.UserResponseDto;
import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long id);

    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto updateUser(Long id, UserRequestDto userRequestDto);

    void deleteUserById(Long id);
}
