package com.blooddonation.controller;

import com.blooddonation.dto.ApiResponse;
import com.blooddonation.dto.AuthDTO;
import com.blooddonation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDTO.AuthResponse>> login(
            @Valid @RequestBody AuthDTO.LoginRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.login(req), "Login successful"));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<AuthDTO.AuthResponse>> registerAdmin(
            @Valid @RequestBody AuthDTO.RegisterRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.registerAdmin(req), "Admin registered successfully"));
    }
}
