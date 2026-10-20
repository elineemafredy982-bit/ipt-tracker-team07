package tz.ac.dit.ipttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AssessmentRequest(
        @Schema(example = "2026-11-10") @NotNull LocalDate visitDate,
        @Schema(example = "85") @NotNull @Min(0) @Max(100) Integer score,
        @Schema(example = "Good progress, strong teamwork") @Size(max = 2000) String remarks) {
}
