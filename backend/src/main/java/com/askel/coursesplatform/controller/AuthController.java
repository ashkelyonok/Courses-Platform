package com.askel.coursesplatform.controller;

import com.askel.coursesplatform.model.dto.request.AdminAuthRequestDto;
import com.askel.coursesplatform.model.dto.request.AuthRequestDto;
import com.askel.coursesplatform.model.dto.request.LoginRequestDto;
import com.askel.coursesplatform.model.dto.response.AuthResponseDto;
import com.askel.coursesplatform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth controller", description = "Login and register operations")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registration of the user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody @Valid AuthRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/register/admin")
    @Operation(summary = "Register new admin (requires special key)")
    public ResponseEntity<AuthResponseDto> registerAdmin(
            @Valid @RequestBody AdminAuthRequestDto request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    @Operation(summary = "Authorization of the user")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
