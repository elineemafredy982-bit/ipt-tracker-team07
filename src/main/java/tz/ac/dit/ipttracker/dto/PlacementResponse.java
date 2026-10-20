package tz.ac.dit.ipttracker.dto;

import tz.ac.dit.ipttracker.entity.PlacementStatus;
import java.time.LocalDate;

public record PlacementResponse(Long id, Long studentId, String studentName, Long organisationId,
                                String organisationName, Long industrialSupervisorId,
                                String industrialSupervisorName, Long academicSupervisorId,
                                String academicSupervisorName, LocalDate startDate, LocalDate endDate,
                                String department, PlacementStatus status) {
}
