package tz.ac.dit.ipttracker.dto;

import tz.ac.dit.ipttracker.entity.EntryStatus;
import java.time.Instant;
import java.time.LocalDate;

public record EntryResponse(Long id, Long placementId, LocalDate entryDate, String tasksDone,
                            String skillsLearned, Integer hoursWorked, EntryStatus status,
                            String supervisorComment, Instant submittedAt, Instant reviewedAt) {
}
