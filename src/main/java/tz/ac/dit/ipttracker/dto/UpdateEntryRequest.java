package tz.ac.dit.ipttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEntryRequest(
        @Schema(example = "Configured the switch and documented it") @NotBlank @Size(max = 2000) String tasksDone,
        @Schema(example = "VLANs, documentation") @Size(max = 2000) String skillsLearned,
        @Schema(example = "7") @NotNull @Min(1) @Max(12) Integer hoursWorked) {
}
