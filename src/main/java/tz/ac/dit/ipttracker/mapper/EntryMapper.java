package tz.ac.dit.ipttracker.mapper;

import tz.ac.dit.ipttracker.dto.EntryResponse;
import tz.ac.dit.ipttracker.entity.LogbookEntry;

public final class EntryMapper {
    private EntryMapper() {
    }

    public static EntryResponse toResponse(LogbookEntry e) {
        return new EntryResponse(e.getId(), e.getPlacement().getId(), e.getEntryDate(), e.getTasksDone(),
                e.getSkillsLearned(), e.getHoursWorked(), e.getStatus(), e.getSupervisorComment(),
                e.getSubmittedAt(), e.getReviewedAt());
    }
}
