package tz.ac.dit.ipttracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePlacementRequest(
        @Schema(example = "5") @NotNull Long studentId,
        @Schema(example = "1") @NotNull Long organisationId,
        @Schema(example = "2") @NotNull Long industrialSupervisorId,
        @Schema(example = "3") @NotNull Long academicSupervisorId,
        @Schema(example = "2026-10-01") @NotNull LocalDate startDate,
        @Schema(example = "2026-12-20") @NotNull LocalDate endDate,
        @Schema(example = "Software") @NotBlank @Size(max = 100) String department) {
}
