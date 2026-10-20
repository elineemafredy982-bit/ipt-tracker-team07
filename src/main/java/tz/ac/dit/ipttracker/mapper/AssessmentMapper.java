package tz.ac.dit.ipttracker.mapper;

import tz.ac.dit.ipttracker.dto.AssessmentResponse;
import tz.ac.dit.ipttracker.entity.Assessment;

public final class AssessmentMapper {
    private AssessmentMapper() {
    }

    public static AssessmentResponse toResponse(Assessment a) {
        return new AssessmentResponse(a.getId(), a.getPlacement().getId(), a.getAssessor().getId(),
                a.getAssessor().getFullName(), a.getVisitDate(), a.getScore(), a.getRemarks());
    }
}
