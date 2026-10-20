package tz.ac.dit.ipttracker.controller;

import tz.ac.dit.ipttracker.dto.LoginRequest;
import tz.ac.dit.ipttracker.dto.TokenResponse;
import tz.ac.dit.ipttracker.dto.UserResponse;
import tz.ac.dit.ipttracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and current user")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Log in with email and password and receive a JWT")
    @ApiResponse(responseCode = "200", description = "Token issued")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "401", description = "Wrong credentials")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Profile of the logged-in user")
    @ApiResponse(responseCode = "200", description = "Current user")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
