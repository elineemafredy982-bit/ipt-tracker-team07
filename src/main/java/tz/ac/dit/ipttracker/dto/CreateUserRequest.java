package tz.ac.dit.ipttracker.dto;

import tz.ac.dit.ipttracker.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Schema(example = "Asha Mussa") @NotBlank @Size(max = 150) String fullName,
        @Schema(example = "asha@example.com") @NotBlank @Email @Size(max = 150) String email,
        @Schema(example = "Str0ngPassw0rd!") @NotBlank @Size(min = 8, max = 72) String password,
        @Schema(example = "STUDENT") @NotNull Role role,
        @Schema(example = "+255700000000") @Size(max = 30) String phone) {
}
