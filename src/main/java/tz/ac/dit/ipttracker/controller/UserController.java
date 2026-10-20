package tz.ac.dit.ipttracker.controller;

import tz.ac.dit.ipttracker.dto.CreateUserRequest;
import tz.ac.dit.ipttracker.dto.PageResponse;
import tz.ac.dit.ipttracker.dto.UserResponse;
import tz.ac.dit.ipttracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "Account management (ADMIN only)")
public class UserController {

    private final UserService service;

    @PostMapping
    @Operation(summary = "Create a user; the password is stored as a BCrypt hash")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "409", description = "Email already in use")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = service.create(request);
        return ResponseEntity.created(Locations.of("/api/v1/users/{id}", created.id())).body(created);
    }

    @GetMapping
    @Operation(summary = "List users (paged)")
    @ApiResponse(responseCode = "200", description = "A page of users")
    public ResponseEntity<PageResponse<UserResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }
}
