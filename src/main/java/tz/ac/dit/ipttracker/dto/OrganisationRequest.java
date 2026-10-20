package tz.ac.dit.ipttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganisationRequest(
        @Schema(example = "TTCL") @NotBlank @Size(max = 200) String name,
        @Schema(example = "ICT") @NotBlank @Size(max = 100) String sector,
        @Schema(example = "Dar es Salaam") @NotBlank @Size(max = 100) String region,
        @Schema(example = "Samora Avenue") @Size(max = 255) String address,
        @Schema(example = "hr@ttcl.test") @Email @Size(max = 150) String contactEmail) {
}
