package tz.ac.dit.ipttracker.dto;

import java.time.LocalDate;

public record AssessmentResponse(Long id, Long placementId, Long assessorId, String assessorName,
                                 LocalDate visitDate, Integer score, String remarks) {
}
