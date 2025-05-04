package com.askel.coursesplatform.service.impl;

import com.askel.coursesplatform.model.dto.request.AdminAuthRequestDto;
import com.askel.coursesplatform.model.dto.request.AuthRequestDto;
import com.askel.coursesplatform.model.dto.request.LoginRequestDto;
import com.askel.coursesplatform.model.dto.response.AuthResponseDto;
import com.askel.coursesplatform.model.entity.User;
import com.askel.coursesplatform.model.enums.UserRoles;
import com.askel.coursesplatform.repository.UserRepository;
import com.askel.coursesplatform.security.JwtService;
import com.askel.coursesplatform.service.AuthService;
import com.askel.coursesplatform.service.validation.AdminKeyValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AdminKeyValidator adminKeyValidator;

    @Transactional
    public AuthResponseDto register(AuthRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRoles.USER);

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token);
    }

    @Override
    public AuthResponseDto registerAdmin(AdminAuthRequestDto request) {
        adminKeyValidator.validate(request.adminKey());

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRoles.ADMIN);

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token);
    }
}
