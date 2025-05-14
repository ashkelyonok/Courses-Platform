package com.askel.coursesplatform.service;

import com.askel.coursesplatform.model.dto.request.AdminAuthRequestDto;
import com.askel.coursesplatform.model.dto.request.AuthRequestDto;
import com.askel.coursesplatform.model.dto.request.LoginRequestDto;
import com.askel.coursesplatform.model.dto.response.AuthResponseDto;
import com.askel.coursesplatform.model.enums.UserRoles;

public interface AuthService {
    AuthResponseDto register(AuthRequestDto request);

    AuthResponseDto registerAdmin(AdminAuthRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}
