package tz.ac.dit.ipttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateEntryRequest(
        @Schema(example = "2026-10-19") @NotNull @PastOrPresent LocalDate entryDate,
        @Schema(example = "Configured the office network switch") @NotBlank @Size(max = 2000) String tasksDone,
        @Schema(example = "VLANs and basic routing") @Size(max = 2000) String skillsLearned,
        @Schema(example = "8") @NotNull @Min(1) @Max(12) Integer hoursWorked) {
}
