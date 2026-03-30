package com.blooddonation.dto;

import com.blooddonation.model.Donor;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

// ───────────────────────────────────────────────
// Auth DTOs
// ───────────────────────────────────────────────

public class AuthDTO {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;

        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 6)
        private String password;

        @NotBlank
        private String phone;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private String id;
        private String name;
        private String email;
        private List<String> roles;

        public AuthResponse(String token, String id, String name, String email, List<String> roles) {
            this.token = token;
            this.id = id;
            this.name = name;
            this.email = email;
            this.roles = roles;
        }
    }
}
