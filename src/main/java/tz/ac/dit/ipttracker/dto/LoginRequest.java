package tz.ac.dit.ipttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "student1@ipt.test") @NotBlank @Email String email,
        @Schema(example = "Password123!") @NotBlank String password) {
}
