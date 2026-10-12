package com.bloodify.controller;

import com.bloodify.model.AppUser;
import com.bloodify.repository.AppUserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 60)
            @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username may contain letters, digits, . _ -")
            String username,
            @NotBlank @Size(min = 10, max = 100, message = "Password must be 10-100 characters")
            String password) {}

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AdminUserController(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /** Admin only: creates a STAFF account. */
    @PostMapping("/users")
    public ResponseEntity<?> createStaff(@Valid @RequestBody CreateUserRequest req) {
        String username = req.username().trim();
        if (users.existsByUsername(username)) {
            return ResponseEntity.status(409).body(Map.of("error", "Username already exists"));
        }
        users.save(new AppUser(username, encoder.encode(req.password()), "STAFF"));
        return ResponseEntity.status(201).body(Map.of("username", username, "role", "STAFF"));
    }
}