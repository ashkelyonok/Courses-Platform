package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.dto.request.AuthRequestDto;
import com.askel.coursesplatform.model.dto.request.LoginRequestDto;
import com.askel.coursesplatform.model.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto register(AuthRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}
